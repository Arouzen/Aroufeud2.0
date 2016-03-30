package aroufeud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import managers.GameManager;
import objects.Game;

/**
 *
 * @author arouz
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to Aroufued2.0!");
        /*while (true) {
            System.out.println("Select a action by typing the correspondent action id:");
            System.out.println("[1] Check status");
            System.out.println("[2] Check game");
            System.out.println("[1] Check status");
            System.out.println("[1] Check status");
        }*/

        String my_username = "lightpepsii";
        String email = "lightpepsii@gmail.com";
        String password = "bajsapa123";
        Aroufeud aroufeud = new Aroufeud();
        GameManager gm = new GameManager();

        // Login
        boolean success;
        try {
            success = aroufeud.login(email, password);
        } catch (Exception ex) {
            ex.printStackTrace();
            success = false;
        }
        if (success) {
            // Login successful
            System.out.println("Login successful!");
            try {
                // Parse account status, parse games into a arraylist
                ArrayList<Game> gameList = aroufeud.parseStatus(my_username);
                
                // FOR DEBUGGING. SAVE A GAME OBJECT TO troll.ser, DEBUG THE WORD BRUTEFORCER WITH THE OBJECT INSTEAD OF INTERNET CALLS EVERY SINGLE REEXECUTION
                Game game = gameList.get(0);
                
                FileOutputStream fout = null;
                try {
                    fout = new FileOutputStream(new File("./troll.ser"));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(fout);
                } catch (IOException ex) {
                    Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    oos.writeObject(game);
                } catch (IOException ex) {
                    Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                //gm.parseGameList(gameList);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
