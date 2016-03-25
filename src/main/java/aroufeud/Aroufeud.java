package aroufeud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import managers.GameManager;
import managers.SessionManager;
import objects.Game;
import org.json.JSONArray;
import wordtrie.WordTrie;
import org.json.JSONObject;

/**
 *
 * @author arouz
 */
public class Aroufeud {

    SessionManager sm;

    public Aroufeud() {
        sm = new SessionManager();
    }

    public boolean login(String email, String password) throws Exception {
        return sm.doLogin(email, password);
    }

    public ArrayList<Game> parseStatus(String my_username) throws Exception {
        JSONObject response = sm.getStatus();
        System.out.println(response.toString());
        if (response.get("status").equals("success")) {
            response = (JSONObject) response.get("content");
            parseInvites((JSONArray) response.get("invites_received"));
            return parseGames((JSONArray) response.get("games"), my_username);
        }
        return null;
    }

    private void parseInvites(JSONArray inviteArray) {
        /*for (JSONObject invite : inviteArray) {
            
        }*/
        System.out.println("Pending incoming invites: " + inviteArray.length());
    }

    private ArrayList<Game> parseGames(JSONArray gameArray, String my_username) {
        String ids = "";
        for (Object game : gameArray) {
            String id = String.valueOf(((JSONObject) game).get("id"));
            ids += id + ",";
        }
        // Remove last comma
        ids = ids.substring(0, ids.length()-1);

        JSONObject gameDetails = null;
        try {
            gameDetails = sm.getGameDetails(ids);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        ArrayList<Game> gameList = new ArrayList<>();
        
        if (gameDetails != null && gameDetails.get("status").equals("success")) {
            gameDetails = (JSONObject) gameDetails.get("content");
            JSONArray games = gameDetails.getJSONArray("games");
            for (Object game : games) {
                gameList.add(new Game((JSONObject) game, my_username));
            }
        }
        
        return gameList;
    }

    public void generateWordTrie() {
        long startTime = System.currentTimeMillis();
        File ordlista = new File("wordlist.txt");
        WordTrie trie = new WordTrie();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ordlista), "UTF8"))) {
            for (String line; (line = br.readLine()) != null;) {
                if (!line.startsWith("#")) {
                    trie.addWord(line.toLowerCase());
                }
            }
        } catch (IOException ex) {
            System.out.println("wordlist.txt wasn't found.");
        } finally {
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("Trie generated in: " + totalTime + "ms.");
        }
        List list = trie.getWords("aba");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
}
