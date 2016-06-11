/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf31kochfractalfx;

import calculate.Edge;
import calculate.KochSide;
import calculate.KochFractal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import static java.lang.Thread.sleep;

/**
 *
 * @author Robin de Kort / Mario Schipper
 */
public class KochTask extends Task<List<Edge>> implements Observer {
	
	private JSF31KochFractalFX application;
	private KochSide side;
	private KochFractal kochFractal;
	private List<Edge> edges;
	
    private static final Logger LOG = Logger.getLogger(KochTask.class.getName());
	
	public KochTask(int level, KochSide side, JSF31KochFractalFX application) {
		this.kochFractal = new KochFractal();
		this.application = application;
		this.side = side;
		this.edges = new ArrayList();
		
		kochFractal.addObserver(this);
		kochFractal.setLevel(level);
		updateProgress(0, kochFractal.getNrOfEdges() / 3);
		updateMessage("0");
	}

	@Override
	public List<Edge> call() throws InterruptedException {
		switch (side) {
			case B :
				kochFractal.generateBottomEdge(edges);
				break;
			case L :
				kochFractal.generateLeftEdge(edges);
				break;
			default :
				kochFractal.generateRightEdge(edges);
				break;
		}
		return edges;
	}

	@Override
	public void update(Observable o, Object arg) {
		Edge edge = (Edge)arg;
		edges.add(edge);
		updateProgress(edges.size(), kochFractal.getNrOfEdges() / 3);
		updateMessage(edges.size() + "");
		
		Edge e = new Edge(edge.X1, edge.Y1, edge.X2, edge.Y2, Color.WHITE);
		application.requestDrawEdge(e);
		
		try {
			sleep(1);
		} catch (InterruptedException ex) {
			Logger.getLogger(KochTask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

    /**
     * Called if execution state is Worker.State CANCELLED
     */
    @Override
    protected void cancelled() {
        super.cancelled();
        kochFractal.cancel();
        LOG.info(side + " cancelled()");
    }

    /**
     * Called if execution state is Worker.State FAILED (see interface
     * Worker<V>)
     */
    @Override
    protected void failed() {
        super.failed();
        LOG.info(side + " failed()");
    }

    /**
     * Called if execution state is Worker.State RUNNING
     */
    @Override
    protected void running() {
        super.running();
        LOG.info(side + " running()");
    }

    /**
     * Called if execution state is Worker.State SCHEDULED
     */
    @Override
    protected void scheduled() {
        super.scheduled();
        LOG.info(side + " scheduled()");
    }

    /**
     * Called if execution state is Worker.State SUCCEEDED
     */
    @Override
    protected void succeeded() {
        super.succeeded();
        LOG.info(side + " succeeded()");
    }

    /**
     * Called if FutureTask behaviour is done
     */
    @Override
    protected void done() {
        super.done();
        LOG.info(side + " done()");
    }
}
