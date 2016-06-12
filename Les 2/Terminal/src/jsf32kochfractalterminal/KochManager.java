/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import calculate.Edge;
import calculate.KochSide;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
		OutputStream fos = null;
		DataOutputStream dos = null;
        try {
            fos = new FileOutputStream(outFile, false);
			dos = new DataOutputStream(fos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
		
        try {
            dos.writeInt(kochLevel);
			for (Edge e : edgeList) {
				dos.writeDouble(e.X1);
				dos.writeDouble(e.Y1);
				dos.writeDouble(e.X2);
				dos.writeDouble(e.Y2);
				dos.writeDouble(e.color.getHue());
				dos.writeDouble(e.color.getSaturation());
				dos.writeDouble(e.color.getBrightness());
			}
			dos.close();
			fos.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	private void saveEdgesBinaryBuffered() {
		setFile("edges.bedg");
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		DataOutputStream dos = null;
        try {
            fos = new FileOutputStream(outFile, false);
			bos = new BufferedOutputStream(fos);
			dos = new DataOutputStream(bos);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
		
        try {
            dos.writeInt(kochLevel);
			for (Edge e : edgeList) {
				dos.writeDouble(e.X1);
				dos.writeDouble(e.Y1);
				dos.writeDouble(e.X2);
				dos.writeDouble(e.Y2);
				dos.writeDouble(e.color.getHue());
				dos.writeDouble(e.color.getSaturation());
				dos.writeDouble(e.color.getBrightness());
			}
			dos.close();
			bos.close();
			fos.close();
        } catch (IOException ex) {
            Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
	private void saveEdgesText() {
		setFile("edges.tedg");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outFile, "UTF-8");
		} catch (FileNotFoundException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			return;
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		
		pw.println(kochLevel);
		for (Edge e : edgeList) {
			String edgeString = String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s", e.X1, e.Y1, e.X2, e.Y2, e.color.getHue(), e.color.getSaturation(), e.color.getBrightness());
//			System.out.println(edgeString);
			pw.println(edgeString);
		}
		pw.close();
	}
	
	private void saveEdgesTextBuffered() {
		setFile("edges.tedg");
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(outFile, false);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			return;
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			return;
		} catch (IOException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			return;
		}
		
		pw.println(kochLevel);
		for (Edge e : edgeList) {
			String edgeString = String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s", e.X1, e.Y1, e.X2, e.Y2, e.color.getHue(), e.color.getSaturation(), e.color.getBrightness());
//			System.out.println(edgeString);
			pw.println(edgeString);
		}
		try {
			pw.close();
			bw.close();
			fw.close();
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
			if (type == BINARY) {
				ts.setBegin("Start writing binary");
				saveEdgesBinary();
				ts.setEnd("Stop writing binary");
			} else if (type == TEXT) {
				ts.setBegin("Start writing text");
				saveEdgesText();
				ts.setEnd("Stop writing text");
			} else if (type == BINARYBUFFERED) {
				ts.setBegin("Start writing binary buffered");
				saveEdgesBinaryBuffered();
				ts.setEnd("Stop writing binary buffered");
			} else if (type == TEXTBUFFERED) {
				ts.setBegin("Start writing text buffered");
				saveEdgesTextBuffered();
				ts.setEnd("Stop writing text buffered");
			}
			System.out.println(ts.toString());
			pool.shutdown();
		}
	}

	public void stop() {
		pool.shutdownNow();
	}
}
