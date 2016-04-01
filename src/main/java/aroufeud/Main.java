package aroufeud;

import java.io.Console;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import managers.GameManager;
import objects.Game;
import org.json.JSONObject;

/**
 *
 * @author arouz
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("Welcome to Aroufued2.0!");
        System.out.println("Please authenticate to the WordFeud servers.");
        ExecutorService pool = null;
        while (true) {
            Scanner scan = new Scanner(System.in);
            System.out.print("Email: ");
            String my_email = scan.next();
            System.out.print("Password: ");
            Console cons = System.console();
            String password;
            if (cons == null) {
                password = scan.next();
            } else {
                password = new String(cons.readPassword());
            }
            System.out.println("");

            // Try to authenticate
            JSONObject response = null;
            Aroufeud aroufeud = new Aroufeud();
            try {
                response = aroufeud.login(my_email, password);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (response != null && response.getString("status").equals("success")) {
                // Auth successful
                // Init the game manager
                GameManager gm = new GameManager(aroufeud.sm);
                // Init the threadpool
                pool = Executors.newFixedThreadPool(8);
                System.out.println("Authentication successful!");
                System.out.println("Semi automatic or fully automatic moves? s/f");
                String auto = scan.next();
                boolean fullyAuto = false;
                if (auto.equalsIgnoreCase("f")) {
                    fullyAuto = true;
                }
                while (true) {
                    try {
                        // Parse account status, parse invites, accept english/swedish normal game invites, decline the rest, parse the games, then play the games where it's your turn
                        ArrayList<Game> gameList = aroufeud.parseStatus(((JSONObject) response.get("content")).getString("username"));
                        gm.playGames(gameList, fullyAuto, pool);
                        System.out.println("----------");
                        System.out.println("Sleeping for 60seconds...");
                        Thread.sleep(60 * 60 * 60);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                System.out.println("Retry? y/n");
                String choice = scan.next();
                if (!choice.equalsIgnoreCase("y")) {
                    break;
                }
            }
        }
        if (pool != null) {
            pool.shutdown();
        }
    }
}
