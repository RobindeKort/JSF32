/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalfx;

import calculate.Edge;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
					final TimeStamp ts = new TimeStamp();
					ts.setBegin("Start loading Bin");
					FileInputStream fis = new FileInputStream(file);
                    DataInputStream dis = new DataInputStream(fis);
                    final int level = dis.readInt();
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
                        Edge e = new Edge(dis.readDouble(),dis.readDouble(),
										  dis.readDouble(),dis.readDouble(),
										  Color.hsb(dis.readDouble(),dis.readDouble(),dis.readDouble()));
                        application.requestDrawEdge(e);
						tempEdgeList.add(e);
                    }
					edgeList = tempEdgeList;
					dis.close();
					fis.close();
					ts.setEnd("Stop loading Bin");
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							application.setTextCalc(ts.toString());
						}
					});
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                }
			}
		}).start();
	}
	
	public void loadBinaryBuffered(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					tempEdgeList.clear();
					final TimeStamp ts = new TimeStamp();
					ts.setBegin("Start loading BinBuf");
					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);
                    DataInputStream dis = new DataInputStream(bis);
                    final int level = dis.readInt();
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
                        Edge e = new Edge(dis.readDouble(),dis.readDouble(),
										  dis.readDouble(),dis.readDouble(),
										  Color.hsb(dis.readDouble(),dis.readDouble(),dis.readDouble()));
                        application.requestDrawEdge(e);
						tempEdgeList.add(e);
                    }
					edgeList = tempEdgeList;
					dis.close();
					bis.close();
					fis.close();
					ts.setEnd("Stop loading BinBuf");
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							application.setTextCalc(ts.toString());
						}
					});
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
					final TimeStamp ts = new TimeStamp();
					ts.setBegin("Start loading Txt");
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
					ts.setEnd("Stop loading Txt");
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							application.setTextCalc(ts.toString());
						}
					});
				} catch (FileNotFoundException ex) {
					Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}).start();
	}
	
	public void loadTextBuffered(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					tempEdgeList.clear();
					final TimeStamp ts = new TimeStamp();
					ts.setBegin("Start loading TxtBuf");
					FileReader fr = new FileReader(file);
					BufferedReader br = new BufferedReader(fr);
					Scanner sc = new Scanner(br);
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
					br.close();
					fr.close();
					ts.setEnd("Stop loading TxtBuf");
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							application.setTextCalc(ts.toString());
						}
					});
				} catch (FileNotFoundException ex) {
					Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
				} catch (IOException ioe) {
					Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ioe);
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
