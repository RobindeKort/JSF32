/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalfx;

import calculate.Edge;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;

/**
 *
 * @author Robin
 */
public class ServerListener extends Observable implements Runnable {

	private boolean listening = true;
	private ObjectInputStream ois;

	public ServerListener(Socket socket) throws IOException {
		ois = new ObjectInputStream(socket.getInputStream());
	}

	@Override
	public void run() {
		double x1 = 0, y1 = 0, x2 = 0, y2 = 0, hue = 0, sat = 0, bri = 0;
		try {
			while (listening) {
				x1 = ois.readDouble();
				y1 = ois.readDouble();
				x2 = ois.readDouble();
				y2 = ois.readDouble();
				hue = ois.readDouble();
				sat = ois.readDouble();
				bri = ois.readDouble();
				Edge edge = new Edge(x1, y1, x2, y2, Color.hsb(hue, sat, bri));
				this.setChanged();
				this.notifyObservers(edge);
			}
		} catch (IOException ex) {
			Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println("Disconnected");
		}
	}

	public void stopListening() {
		listening = false;
	}
}
