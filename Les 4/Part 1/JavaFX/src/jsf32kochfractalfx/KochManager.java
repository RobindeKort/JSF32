/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalfx;

import calculate.Edge;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import timeutil.TimeStamp;
import watchservice.WatchDirRunnable;

/**
 * @author Robin de Kort / Mario Schipper
 */
public class KochManager {

	// The JavaFX application
	private KochFractalFX application;
	private List<Edge> edgeList;
	private List<Edge> tempEdgeList;
	private File inFile;
	private WatchDirRunnable watchRunnable;
	private Thread watchThread;

	public KochManager(KochFractalFX application) {
		this.application = application;
		this.edgeList = new ArrayList<Edge>();
		this.tempEdgeList = new ArrayList<Edge>();
		Path dir = Paths.get(System.getProperty("user.home"));
		try {
			this.watchRunnable = new WatchDirRunnable(dir, false, this);
			watchThread = new Thread(watchRunnable);
			watchThread.start();
		} catch (IOException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(-1);
		}
	}
	
	public void loadBinary(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					tempEdgeList.clear();
					final TimeStamp ts = new TimeStamp();
					ts.setBegin("Start loading Bin");
					RandomAccessFile raf = new RandomAccessFile(file, "rw");
					// Only read the level which is an int (int=4 bytes)
					final int level = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, 4).getInt();
                    final int nrOfEdges = (int) (3 * Math.pow(4, level - 1));
					// 8 bytes per double per edge (has 7 doubles)
					int NUMBER_OF_BYTES = nrOfEdges*7*8;
					FileChannel fc = raf.getChannel();
					MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 4, NUMBER_OF_BYTES);
                    Platform.runLater(new Runnable() {
						@Override
						public void run() {
							application.clearKochPanel();
							application.setTextLevel(level);
							application.setTextNrEdges("Nr of edges: " + nrOfEdges);
						}
					});
					
                    for(int i = 0; i<nrOfEdges; i++){
                        Edge e = new Edge(mbb.getDouble(),mbb.getDouble(),
										  mbb.getDouble(),mbb.getDouble(),
										  Color.hsb(mbb.getDouble(),mbb.getDouble(),mbb.getDouble()));
                        application.requestDrawEdge(e);
						tempEdgeList.add(e);
                    }
					edgeList = tempEdgeList;
					fc.close();
					raf.close();
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

	public void fileCreated() {
		inFile = new File(System.getProperty("user.home"), "finishedEdges.bedg");
		this.loadBinary(inFile);
		System.gc();
	}

	public void fileModified() {
		
	}

	public void fileDeleted() {
		// Nothing to do here
	}
	
	public void stop() {
		watchRunnable.stop();
		watchThread.interrupt();
	}
}
