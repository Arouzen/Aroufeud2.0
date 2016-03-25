/*
 */
package aroufeud;

import java.util.ArrayList;
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
                gm.parseGameList(gameList);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
