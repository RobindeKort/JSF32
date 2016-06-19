/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import calculate.Edge;
import calculate.KochSide;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import timeutil.TimeStamp;

/**
 *
 * @author Robin de Kort / Mario Schipper
 */
public class KochManager implements Observer {

	// A double is 8 bytes and the Edge we save/load consists of 7 doubles. 
	private final int EDGE_BYTE_SIZE = 8*7;
	private int NUMBER_OF_EDGES;

	private List<Edge> edgeList;

	private ExecutorService pool;
	private KochRunnable runLeft;
	private KochRunnable runRight;
	private KochRunnable runBottom;

	private int kochLevel;
	private TimeStamp timeStamp;
	private int finishedThreadCount;

	private File outFile;

	public KochManager() {
		edgeList = new ArrayList<Edge>();
		pool = Executors.newFixedThreadPool(3);
		runLeft = null;
		runRight = null;
		runBottom = null;
		finishedThreadCount = 0;
	}

	private boolean setFile(String filename) {
		try {
			outFile = new File(System.getProperty("user.home"), filename);
			if (outFile.exists()) {
				outFile.delete();
			}
			if (!outFile.createNewFile()) {
				outFile = null;
				return false;
			}
			if (!outFile.canWrite()) {
				outFile = null;
				return false;
			}
			return true;
		} catch (IOException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			outFile = null;
			return false;
		}
	}

	public void changeLevel(final int level) {
		// 4 bytes per int (level) and 8 bytes per double per edge (has 7 doubles)
		NUMBER_OF_EDGES = (int) (3 * Math.pow(4, level - 1));
		
		pool.shutdownNow();
		pool = Executors.newFixedThreadPool(3);
		kochLevel = level;
		finishedThreadCount = 0;

		timeStamp = new TimeStamp();
		timeStamp.setBegin("Start calculating");

		runLeft = new KochRunnable(kochLevel, KochSide.L);
		runLeft.addObserver(this);
		runRight = new KochRunnable(kochLevel, KochSide.R);
		runRight.addObserver(this);
		runBottom = new KochRunnable(kochLevel, KochSide.B);
		runBottom.addObserver(this);

		pool.submit(runLeft);
		pool.submit(runRight);
		pool.submit(runBottom);
	}

	private synchronized void addEdges(List<Edge> edges) {
		edgeList.addAll(edges);
		finishedThreadCount++;
		System.out.println("Threads finished: " + finishedThreadCount);
	}

	private void saveEdgesBinary() {
		setFile("edges.bedg");
		RandomAccessFile raf = null;
		FileChannel fc = null;
		MappedByteBuffer mbb = null;
		FileLock flock = null;
		try {
			raf = new RandomAccessFile(outFile, "rw");
			fc = raf.getChannel();
			
			mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, 4);
			mbb.putInt(kochLevel);
			
			for (int i = 0; i < NUMBER_OF_EDGES; i++) {
				int writePos = 8 + EDGE_BYTE_SIZE*i;
				System.out.format("Writing to position: %1$s\n", writePos);
				mbb = fc.map(FileChannel.MapMode.READ_WRITE, writePos, EDGE_BYTE_SIZE);
				flock = fc.lock(writePos, EDGE_BYTE_SIZE, false);
				
				Edge e = edgeList.get(i);
				mbb.putDouble(e.X1);
				mbb.putDouble(e.Y1);
				mbb.putDouble(e.X2);
				mbb.putDouble(e.Y2);
				mbb.putDouble(e.color.getHue());
				mbb.putDouble(e.color.getSaturation());
				mbb.putDouble(e.color.getBrightness());
				
				flock.release();
				mbb = fc.map(FileChannel.MapMode.READ_WRITE, 4, 4);
				flock = fc.lock(4, 4, true);
				mbb.putInt(i);
				flock.release();
			}
			fc.close();
			raf.close();
		} catch (IOException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		List<Edge> edges = (List<Edge>) arg;
		addEdges(edges);
		if (finishedThreadCount >= 3) {
			timeStamp.setEnd("Stop calculating");
			System.out.println(timeStamp.toString());

			TimeStamp ts = new TimeStamp();
			ts.setBegin("Start writing binary");
			saveEdgesBinary();
			ts.setEnd("Stop writing binary");
			System.out.println(ts.toString());

			pool.shutdown();
		}
	}

	public void stop() {
		pool.shutdownNow();
	}
}
