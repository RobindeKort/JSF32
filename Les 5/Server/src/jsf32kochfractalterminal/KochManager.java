/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import calculate.Edge;
import calculate.KochSide;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
import javafx.scene.paint.Color;
import static jsf32kochfractalterminal.SendType.*;
import timeutil.TimeStamp;

/**
 *
 * @author Robin de Kort / Mario Schipper
 */
public class KochManager implements Observer {

	private List<Edge> edgeList;
	private ExecutorService pool;
	private KochRunnable runLeft;
	private KochRunnable runRight;
	private KochRunnable runBottom;

	private int NUMBER_OF_EDGES;
	private int kochLevel;
	private TimeStamp timeStamp;

	private SendType sendType;
	private ObjectOutputStream oos;
	private String fileName = "edges";
	private File outFile;

	public KochManager(ObjectOutputStream oos) {
		edgeList = new ArrayList();
		pool = Executors.newFixedThreadPool(3);
		runLeft = null;
		runRight = null;
		runBottom = null;
		this.oos = oos;

		for (int i = 0; i < Double.POSITIVE_INFINITY; i++) {
			if (!new File(fileName + ".bedg").exists()) {
				break;
			}
			fileName = "edges" + i;
		}
	}

	public int getKochLevel() {
		return kochLevel;
	}

	public void setSendType(SendType sendType) {
		this.sendType = sendType;
	}

	private boolean setFile(String filename) {
		try {
			outFile = new File(filename);
			outFile.deleteOnExit();
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
		edgeList = new ArrayList();
		kochLevel = level;

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

	public void loadEdgesBinary() {
		RandomAccessFile raf = null;
		FileChannel fc = null;
		MappedByteBuffer mbb = null;
		try {
			raf = new RandomAccessFile(outFile, "rw");
			fc = raf.getChannel();
			mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, NUMBER_OF_EDGES * 7 * 8);
			for (int i = 0; i < NUMBER_OF_EDGES * 7; i++) {
				oos.writeDouble(mbb.getDouble());
			}
			oos.flush();
			fc.close();
			raf.close();
		} catch (IOException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void saveEdgesBinary() {
		TimeStamp ts = new TimeStamp();
		ts.setBegin("Start writing binary");

		setFile(fileName + ".bedg");
		RandomAccessFile raf = null;
		FileChannel fc = null;
		MappedByteBuffer mbb = null;
		try {
			raf = new RandomAccessFile(outFile, "rw");
			fc = raf.getChannel();
			mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, NUMBER_OF_EDGES * 7 * 8);
			for (Edge e : edgeList) {
				mbb.putDouble(e.X1);
				mbb.putDouble(e.Y1);
				mbb.putDouble(e.X2);
				mbb.putDouble(e.Y2);
				mbb.putDouble(e.color.getHue());
				mbb.putDouble(e.color.getSaturation());
				mbb.putDouble(e.color.getBrightness());
			}
			fc.close();
			raf.close();
		} catch (IOException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		ts.setEnd("Stop writing binary");
		System.out.println(ts.toString());
	}

	private synchronized void addEdge(Edge e) {
		edgeList.add(e);
//		System.out.format("%1$s/%2$s\n", edgeList.size(), NUMBER_OF_EDGES);
		if (sendType == SINGLE) {
			System.out.println("WRITING SINGLE");
			try {
				oos.writeDouble(e.X1);
				oos.writeDouble(e.Y1);
				oos.writeDouble(e.X2);
				oos.writeDouble(e.Y2);
				oos.writeDouble(e.color.getHue());
				oos.writeDouble(e.color.getSaturation());
				oos.writeDouble(e.color.getBrightness());
				oos.flush();
			} catch (IOException ex) {
				Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (edgeList.size() == NUMBER_OF_EDGES) {
			timeStamp.setEnd("Stop calculating");
			System.out.println(timeStamp.toString());
			if (sendType == ALL) {
				System.out.println("WRITING ALL");
				try {
					for (Edge edge : edgeList) {
						oos.writeDouble(edge.X1);
						oos.writeDouble(edge.Y1);
						oos.writeDouble(edge.X2);
						oos.writeDouble(edge.Y2);
						oos.writeDouble(edge.color.getHue());
						oos.writeDouble(edge.color.getSaturation());
						oos.writeDouble(edge.color.getBrightness());
					}
					oos.flush();
				} catch (IOException ex) {
					Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			saveEdgesBinary();
			System.gc();
			System.out.format("Sent %1$s edges for level %2$s\n", edgeList.size(), kochLevel);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Edge edge = (Edge) arg;
		addEdge(edge);
	}

	public void stop() {
		pool.shutdownNow();
	}
}
