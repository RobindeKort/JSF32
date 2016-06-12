/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf32kochfractalterminal;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jsf32kochfractalterminal.FileType.*;

/**
 *
 * @author Robin, Mario
 */
public class KochFractalTerminal {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
		
        int level = 0;
        while (level <= 0 || level > 12) {
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
		
		FileType type;
		while (true) {
			System.out.print("Write as (b)inary or (t)ext: ");
            String input = sc.nextLine();
            // Sleep because otherwise the output gets messed up for some reason
            // Probably because of the VM
            Thread.sleep(1);
			if (input.trim().toLowerCase().equals("b")){
				type = BINARY;
				break;
			} else if (input.trim().toLowerCase().equals("t")) {
				type = TEXT;
				break;
			} else {
				System.out.println("Invalid input");
			}
		}
		
		while (true) {
			System.out.print("Write (b)uffered or (n)on-buffered: ");
            String input = sc.nextLine();
            // Sleep because otherwise the output gets messed up for some reason
            // Probably because of the VM
            Thread.sleep(1);
			if (input.trim().toLowerCase().equals("b")){
				if (type == BINARY) {
					type = BINARYBUFFERED;
					break;
				} else {
					type = TEXTBUFFERED;
					break;
				}
			} else if (input.trim().toLowerCase().equals("n")) {
				break;
			} else {
				System.out.println("Invalid input");
			}
		}
		
		KochManager manager = new KochManager(type);
		manager.changeLevel(level);
    }
}
