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
        String my_username = "lightpepsii";
        String email = "lightpepsii@gmail.com";
        String password = "bajsapa123";
        Aroufeud aroufeud = new Aroufeud();
        GameManager gm = new GameManager(aroufeud.sm);

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
                gm.playGames(gameList);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
