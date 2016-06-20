/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import calculate.Edge;
import calculate.KochSide;
import calculate.KochFractal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import static java.lang.Thread.sleep;

/**
 *
 * @author Robin de Kort / Mario Schipper
 */
public class KochRunnable extends Observable implements Observer, Runnable {

	private KochSide side;
	private KochFractal kochFractal;
	private List<Edge> edges;

	public KochRunnable(int level, KochSide side) {
		this.kochFractal = new KochFractal();
		this.side = side;
		this.edges = new ArrayList();

		kochFractal.addObserver(this);
		kochFractal.setLevel(level);
	}

	@Override
	public void run() {
		switch (side) {
			case B:
				kochFractal.generateBottomEdge(edges);
				break;
			case L:
				kochFractal.generateLeftEdge(edges);
				break;
			default:
				kochFractal.generateRightEdge(edges);
				break;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
//		Edge edge = (Edge) arg;
//		System.out.format("%1$s, %2$s, %3$s, %4$s (%5$s, %6$s, %7$s)\n",
//				edge.X1, edge.Y1, edge.X2, edge.Y2,
//				edge.color.getHue(), edge.color.getSaturation(), edge.color.getBrightness());
		this.setChanged();
		this.notifyObservers(arg);

		try {
			sleep(1);
		} catch (InterruptedException ex) {
			Logger.getLogger(KochRunnable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
