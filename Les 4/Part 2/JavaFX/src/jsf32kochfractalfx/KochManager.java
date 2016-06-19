/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalfx;

import calculate.Edge;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import timeutil.TimeStamp;

/**
 * @author Robin de Kort / Mario Schipper
 */
public class KochManager {

	// A double is 8 bytes and the Edge we save/load consists of 7 doubles. 
	private final int EDGE_BYTE_SIZE = 8*7;
	private KochFractalFX application;
	private List<Edge> edgeList;
	private List<Edge> tempEdgeList;
	private File inFile;
	private Thread readingThread;

	public KochManager(KochFractalFX application) {
		this.application = application;
		this.edgeList = new ArrayList<Edge>();
		this.tempEdgeList = new ArrayList<Edge>();
		inFile = new File(System.getProperty("user.home"), "edges.bedg");
		inFile.deleteOnExit();
		startReadingWithLock();
	}
	
	private void startReadingWithLock() {
		readingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (!inFile.exists()){
						//wait
					}
					FileLock flock = null;
					tempEdgeList.clear();
					final TimeStamp ts = new TimeStamp();
					ts.setBegin("Start loading Bin");
					RandomAccessFile raf = new RandomAccessFile(inFile, "rw");
					FileChannel fc = raf.getChannel();
					MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, 4);
					// Only read the level which is an int (int=4 bytes)
					final int level = mbb.getInt();
					final int nrOfEdges = (int) (3 * Math.pow(4, level - 1));
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							application.clearKochPanel();
							application.setTextLevel(level);
							application.setTextNrEdges("Nr of edges: " + nrOfEdges);
						}
					});
					
					for (int i = 0; i < nrOfEdges; i++) {
						mbb = fc.map(FileChannel.MapMode.READ_ONLY, 4, 4);
						flock = fc.lock(4, 4, true);
						while (i-1 >= mbb.getInt()) {
							mbb.clear();
						}
						flock.release();
						int readingPos = 8 + EDGE_BYTE_SIZE*i;
						System.out.format("Reading position: %1$s\n", readingPos);
						mbb = fc.map(FileChannel.MapMode.READ_ONLY, readingPos, EDGE_BYTE_SIZE);
						flock = fc.lock(readingPos, EDGE_BYTE_SIZE, false);
						Edge e = new Edge(mbb.getDouble(),mbb.getDouble(),
										  mbb.getDouble(),mbb.getDouble(),
										  Color.hsb(mbb.getDouble(),mbb.getDouble(),mbb.getDouble()));
                        application.requestDrawEdge(e);
						tempEdgeList.add(e);
						flock.release();
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
				} catch (IOException ioe) {
                    Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ioe);
				}
			}
		});
		readingThread.start();
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
		readingThread.interrupt();
	}
}
