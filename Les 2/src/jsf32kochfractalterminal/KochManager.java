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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jsf32kochfractalterminal.FileType.*;
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

	private int kochLevel;
	private TimeStamp timeStamp;
	private int finishedThreadCount;

	private File outFile;
	private FileType type;

	public KochManager(FileType type) {
		edgeList = new ArrayList<Edge>();
		pool = Executors.newFixedThreadPool(3);
		runLeft = null;
		runRight = null;
		runBottom = null;
		finishedThreadCount = 0;
		setFile("edges.edg");
		this.type = type;
	}

	private boolean setFile(String filename) {
		try {
			outFile = new File(filename);
			if (!outFile.exists() && !outFile.createNewFile()) {
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

	public void changeLevel(final int nxt) {
		pool.shutdownNow();
		pool = Executors.newFixedThreadPool(3);
		kochLevel = nxt;
		finishedThreadCount = 0;

		timeStamp = new TimeStamp();
		timeStamp.setBegin("Start berekenen");

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
		OutputStream out;
        try {
            out = new FileOutputStream(outFile, false);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        DataOutputStream dout = new DataOutputStream(out);
		
        try {
            dout.writeInt(kochLevel);
			for (Edge e : edgeList) {
				dout.writeDouble(e.X1);
				dout.writeDouble(e.Y1);
				dout.writeDouble(e.X2);
				dout.writeDouble(e.Y2);
				dout.writeDouble(e.color.getHue());
				dout.writeDouble(e.color.getSaturation());
				dout.writeDouble(e.color.getBrightness());
			}
			out.close();
			dout.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	private void saveEdgesText() {
		PrintWriter out = null;
		try {
			out = new PrintWriter(outFile, "UTF-8");
		} catch (FileNotFoundException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		out.println(kochLevel);
		for (Edge e : edgeList) {
			String edgeString = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s", e.X1, e.Y1, e.X2, e.Y2, e.color.getHue(), e.color.getSaturation(), e.color.getBrightness());
//			System.out.println(edgeString);
			out.println(edgeString);
		}
		out.close();
	}

	@Override
	public void update(Observable o, Object arg) {
		List<Edge> edges = (List<Edge>) arg;
		addEdges(edges);
		if (finishedThreadCount >= 3) {
			timeStamp.setEnd("Stop berekenen");
			System.out.println(timeStamp.toString());
			TimeStamp ts = new TimeStamp();
			if (type == BINARY) {
				ts.setBegin("Start schrijven binair");
				saveEdgesBinary();
				ts.setEnd("Stop schrijven binair");
			} else if (type == TEXT) {
				ts.setBegin("Start schrijven text");
				saveEdgesText();
				ts.setEnd("Stop schrijven text");
			}
			System.out.println(ts.toString());
			pool.shutdown();
		}
	}

	public void stop() {
		pool.shutdownNow();
	}
}
