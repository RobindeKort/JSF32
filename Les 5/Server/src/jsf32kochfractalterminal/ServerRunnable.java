/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Robin, Mario
 */
public class ServerRunnable implements Runnable {

	private Socket socket;
	private KochManager kochManager;

	public ServerRunnable(Socket socket) {
		System.out.format("Client %1$s connected\n", socket.getInetAddress());
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
								socket.getOutputStream());
			BufferedReader in = new BufferedReader(
								new InputStreamReader(
								socket.getInputStream()));
			kochManager = new KochManager(oos);
			
			String inputLine;
			
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
				String instr = inputLine.trim().toLowerCase();
				if (instr.equals("single")) {
					kochManager.setSendType(SendType.SINGLE);
				} else if (instr.equals("all")) {
					kochManager.setSendType(SendType.ALL);
				} else if (isValidLevel(instr)) {
					int level = Integer.parseInt(instr);
					if (level != kochManager.getKochLevel()) {
						System.out.println("Changing level");
						kochManager.changeLevel(level);
					} else {
						kochManager.loadEdgesBinary();
					}
				} else if (instr.equals("bye")) {
					kochManager.stop();
					break;
				}
			}
			oos.close();
			in.close();
			socket.close();
		} catch (IOException ex) {
			Logger.getLogger(ServerRunnable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private boolean isValidLevel(String input) {
		try{
			int x = Integer.parseInt(input);
			return x >= 1 && x <= 10;
		}
		catch(NumberFormatException e){
		   return false;
		}
	}
}
