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
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import static jsf32kochfractalfx.SendType.*;
import timeutil.TimeStamp;

/**
 *
 * @author Robin de Kort / Mario Schipper
 */
public class KochManager implements Observer {

	// The JavaFX application
	private KochFractalFX application;
	
	private int kochLevel;
	private Socket socket;
	private ServerListener serverListener;
	private Thread listenerThread;
	private PrintWriter printWriter;

	public KochManager(KochFractalFX application) {
		this.application = application;
		kochLevel = 1;
		
		try {
			socket = new Socket("127.0.0.1", 4444);
			printWriter = new PrintWriter(socket.getOutputStream(), true);
			printWriter.println("single");
			serverListener = new ServerListener(socket);
			serverListener.addObserver(this);
			listenerThread = new Thread(serverListener);
			listenerThread.start();
		} catch (IOException ex) {
			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			System.exit(-1);
		}
	}
	
	public void changeLevel(int level) {
		System.out.format("Printing level %1$s\n", level);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				application.clearKochPanel();
			}
		});
		kochLevel = level;
		printWriter.println(level);
	}
	
	public void changeSendType(SendType type) {
		System.out.format("Printing type %1$s\n", type);
		if (type == SINGLE) {
			printWriter.println("single");
		} else if (type == ALL) {
			printWriter.println("all");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		Edge edge = (Edge) arg;
		application.requestDrawEdge(edge);
//		System.out.format("%1$s, %2$s, %3$s, %4$s (%5$s, %6$s, %7$s)\n", 
//						  edge.X1, edge.Y1, edge.X2, edge.Y2, 
//						  edge.color.getHue(), edge.color.getSaturation(), edge.color.getBrightness());
	}
	
	public void stop() {
		try {
			serverListener.stopListening();
			listenerThread.interrupt();
			printWriter.close();
			socket.close();
		} catch (IOException ex) {
//			Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println("Socket closed");
		}
	}
}
