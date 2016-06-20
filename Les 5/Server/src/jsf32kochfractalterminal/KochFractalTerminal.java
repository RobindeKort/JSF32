/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Robin, Mario
 */
public class KochFractalTerminal {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(-1);
        }

		System.out.println("Server started");
        while (listening) {
            // create new Thread which runs Multiserverrunnable
            Thread t = new Thread(new ServerRunnable(serverSocket.accept()));
            // start Thread
            t.start();
        }
        serverSocket.close();
    }
}
