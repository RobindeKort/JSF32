/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import calculate.KochFractal;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class JSF32KochFractalTerminal {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        int level = 0;
        while (level <= 0) {
            System.out.print("KochFractal level: ");
            String input = sc.nextLine();
            // Sleep because otherwise the output gets messed up for some reason
            // Probably because of the VM
            Thread.sleep(1);
            try {
                level = Integer.parseInt(input.trim());
            } catch (NumberFormatException nfe) {
//                Logger.getLogger(JSF32KochFractalTerminal.class.getName()).log(Level.SEVERE, null, nfe);
                System.out.println("Invalid input");
            }
        }

        KochFractal kf = new KochFractal();
        kf.setLevel(level);
    }
}
