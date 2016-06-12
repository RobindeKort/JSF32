/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalfx;

import calculate.Edge;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import timeutil.TimeStamp;

/**
 *
 * @author Robin de Kort / Mario Schipper
 */
public class KochManager {

	// The JavaFX application
	private KochFractalFX application;
	private List<Edge> edgeList;
	private List<Edge> tempEdgeList;

	public KochManager(KochFractalFX application) {
		this.application = application;
		this.edgeList = new ArrayList<Edge>();
		this.tempEdgeList = new ArrayList<Edge>();
	}
	
	public void loadBinary(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					tempEdgeList.clear();
                    DataInputStream inStream = new DataInputStream(new FileInputStream(file));
                    final int level = inStream.readInt();
                    final int nrOfEdges = (int) (3 * Math.pow(4, level - 1));
                    Platform.runLater(new Runnable() {
						@Override
						public void run() {
							application.clearKochPanel();
							application.setTextLevel(level);
							application.setTextNrEdges("Nr of edges: " + nrOfEdges);
						}
					});
					
                    for(int i = 0; i<nrOfEdges; i++){
                        Edge e = new Edge(inStream.readDouble(),inStream.readDouble(),
										  inStream.readDouble(),inStream.readDouble(),
										  Color.hsb(inStream.readDouble(),inStream.readDouble(),inStream.readDouble()));
                        application.requestDrawEdge(e);
						tempEdgeList.add(e);
                    }
					edgeList = tempEdgeList;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                }
			}
		}).start();
	}
	
	public void loadText(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					tempEdgeList.clear();
					Scanner sc = new Scanner(file);
					final int level = sc.nextInt();
					final int nrOfEdges = (int) (3 * Math.pow(4, level - 1));
                    Platform.runLater(new Runnable() {
						@Override
						public void run() {
							application.clearKochPanel();
							application.setTextLevel(level);
							application.setTextNrEdges("Nr of edges: " + nrOfEdges);
						}
					});
					// nextLine to end the current line since only the level is written here
					sc.nextLine();
					
					while (sc.hasNextLine() && sc.hasNextDouble()) {
						Edge e = new Edge(sc.nextDouble(), sc.nextDouble(),
										  sc.nextDouble(), sc.nextDouble(),
										  Color.hsb(sc.nextDouble(), sc.nextDouble(), sc.nextDouble()));
                        application.requestDrawEdge(e);
						tempEdgeList.add(e);
					}
					edgeList = tempEdgeList;
					sc.close();
				} catch (FileNotFoundException ex) {
					Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}).start();
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
}
