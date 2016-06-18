/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import calculate.Edge;
import calculate.KochSide;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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

	private int NUMBER_OF_BYTES;

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
		NUMBER_OF_BYTES = (int) (4 + ((3 * Math.pow(4, level - 1)) * 7 * 8));

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
		MappedByteBuffer mbb = null;
		try {
			raf = new RandomAccessFile(outFile, "rw");
			mbb = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, NUMBER_OF_BYTES);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ioe) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ioe);
		}

		try {
			mbb.putInt(kochLevel);
			for (Edge e : edgeList) {
				mbb.putDouble(e.X1);
				mbb.putDouble(e.Y1);
				mbb.putDouble(e.X2);
				mbb.putDouble(e.Y2);
				mbb.putDouble(e.color.getHue());
				mbb.putDouble(e.color.getSaturation());
				mbb.putDouble(e.color.getBrightness());
			}
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

			// Without the GarbageCollector, the MappedByteBuffer won't close and
			// that will block us from renaming the file. 
			File newFile = new File(System.getProperty("user.home"), "finishedEdges.bedg");
			if (newFile.exists()) {
				newFile.delete();
			}
			System.gc();
			outFile.renameTo(newFile);
		}
	}

	public void stop() {
		pool.shutdownNow();
	}
}
