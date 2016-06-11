/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import calculate.Edge;
import calculate.KochSide;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import timeutil.TimeStamp;

/**
 *
 * @author Robin de Kort / Mario Schipper
 */
public class KochManager {

	// The JavaFX application
	private JSF31KochFractalFX application;
	private List<Edge> edgeList;
	private List<Edge> tempEdgeList;
	
	private ExecutorService pool;

	// Tasks for drawing the edges
	private Task<Void> taskLeft;
	private Task<Void> taskRight;
	private Task<Void> taskBottom;

	private int finishedThreadCount;

	TimeStamp timeStamp;

	public KochManager(JSF31KochFractalFX application) {
		this.application = application;
		
		this.edgeList = new ArrayList<Edge>();
		this.tempEdgeList = new ArrayList<Edge>();
        pool = Executors.newFixedThreadPool(3);
		taskLeft = null;
		taskRight = null;
		taskBottom = null;
	}

	public void changeLevel(final int nxt) {

		if (this.taskLeft != null && this.taskLeft.isRunning()) {
			this.taskLeft.cancel();
			application.progressBarLeft.progressProperty().unbind();
			application.labelProgressLeftEdge.textProperty().unbind();
		}
		if (this.taskRight != null && this.taskRight.isRunning()) {
			this.taskRight.cancel();
			application.progressBarRight.progressProperty().unbind();
			application.labelProgressRightEdge.textProperty().unbind();
		}
		if (this.taskBottom != null && this.taskBottom.isRunning()) {
			this.taskBottom.cancel();
			application.progressBarBottom.progressProperty().unbind();
			application.labelProgressBottomEdge.textProperty().unbind();
		}
		application.requestDrawEdges();

		this.finishedThreadCount = 0;

		timeStamp = new TimeStamp();
		timeStamp.setBegin("Start berekenen");
		
		taskLeft = newTask(nxt, KochSide.L);
		taskRight = newTask(nxt, KochSide.R);
		taskBottom = newTask(nxt, KochSide.B);
		
		// ProgressBars
		application.progressBarLeft.progressProperty().bind(taskLeft.progressProperty());
		application.progressBarRight.progressProperty().bind(taskRight.progressProperty());
		application.progressBarBottom.progressProperty().bind(taskBottom.progressProperty());
		// Labels
		application.labelProgressLeftEdge.textProperty().bind(taskLeft.messageProperty());
		application.labelProgressRightEdge.textProperty().bind(taskRight.messageProperty());
		application.labelProgressBottomEdge.textProperty().bind(taskBottom.messageProperty());
		
		pool.submit(taskLeft);
		pool.submit(taskRight);
		pool.submit(taskBottom);
	}
	
	private Task newTask(int nxt, KochSide side) {
		final Task task = new KochTask(nxt, side, application);
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				tempEdgeList.addAll((Collection<Edge>)task.getValue());
				edgesCalculated();
			}
		});
		task.messageProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int count = Integer.valueOf(application.labelProgressLeftEdge.getText()) + 
							Integer.valueOf(application.labelProgressRightEdge.getText()) + 
							Integer.valueOf(application.labelProgressBottomEdge.getText());
                application.setTextNrEdges(Integer.toString(count));
			}
		});
		return task;
	}

	public void edgesCalculated() {
		finishedThreadCount++;
		System.out.println("Threads finished: " + finishedThreadCount);
		if (finishedThreadCount != 3) {
			return;
		}
//		System.out.println("Threads have finished calculating, starting to draw...");

		timeStamp.setEnd("Stop berekenen");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				application.setTextCalc(timeStamp.toString());
			}
		});

		edgeList.clear();
		edgeList.addAll(tempEdgeList);
		tempEdgeList.clear();
		application.requestDrawEdges();
	}

	public void drawEdges() {
		application.clearKochPanel();
		
		final TimeStamp timeStamp = new TimeStamp();
		timeStamp.setBegin("Start tekenen");
		
		for (Edge edge : edgeList) {
			this.application.drawEdge(edge);
		}
		
		timeStamp.setEnd("Stop tekenen");
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				application.setTextDraw(timeStamp.toString());
			}
		});
	}
	
	public void stop() {
		pool.shutdownNow();
	}
}
