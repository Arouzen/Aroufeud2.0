package aroufeud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import managers.SessionManager;
import objects.Game;
import org.json.JSONArray;
import org.json.JSONObject;
import wordtrie.WordTrie;

/**
 *
 * @author arouz
 */
public final class Aroufeud {

    private final SessionManager sm;
    private WordTrie en_trie;
    private WordTrie sv_trie;
    private HashMap<Character, Integer> sv_charScores;
    private HashMap<Character, Integer> en_charScores;
    private ExecutorService pool;

    public Aroufeud() {
        // Initiate the sessionManager
        sm = new SessionManager();
    }

    public void initialize() {
        // Initiate the tries and scoreMaps
        en_trie = generateWordTrie("en");
        sv_trie = generateWordTrie("sv");
        en_charScores = generateCharMap("en");
        sv_charScores = generateCharMap("sv");
        // Initiate the threadpool
        pool = Executors.newFixedThreadPool(8);
    }

    public JSONObject login(String email, String password) throws Exception {
        return sm.doLogin(email, password);
    }

    public ArrayList<Game> parseStatus(String my_username) throws Exception {
        JSONObject response = sm.getStatus();
        if (response.get("status").equals("success")) {
            response = (JSONObject) response.get("content");
            parseInvites((JSONArray) response.get("invites_received"));
            return parseGames((JSONArray) response.get("games"), my_username);
        } else {
            System.out.println(response.toString());
        }
        return null;
    }

    private void parseInvites(JSONArray inviteArray) {
        for (Object obj : inviteArray) {
            JSONObject invite = (JSONObject) obj;
            if (invite.getString("board_type").equals("normal")) {
                if (invite.getInt("ruleset") == 4 || invite.getInt("ruleset") == 0) {
                    try {
                        sm.acceptInvite(invite.getLong("id"));
                        System.out.println("Accepted invite from " + invite.getString("inviter") + "!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        sm.rejectInvite(invite.getLong("id"));
                        System.out.println("Rejected invite from " + invite.getString("inviter") + "! Reason: ruleset");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    sm.rejectInvite(invite.getLong("id"));
                    System.out.println("Rejected invite from " + invite.getString("inviter") + "! Reason: board_type");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private ArrayList<Game> parseGames(JSONArray gameArray, String my_username) {
        String ids = "";
        for (Object game : gameArray) {
            String id = String.valueOf(((JSONObject) game).get("id"));
            ids += id + ",";
        }
        // Remove last comma
        ids = ids.substring(0, ids.length() - 1);

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

    /**
     * Generate HashMap with a character as key, and its score as value. Values
     * and keys given from external txt file.
     *
     * @return HashMap< Character,Integer >
     */
    private HashMap<Character, Integer> generateCharMap(String language) {
        HashMap<Character, Integer> charMap = new HashMap<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream(language + "_charlist.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"))) {
            for (String line; (line = br.readLine()) != null;) {
                if (!line.startsWith("#")) {
                    // Split row on ":"
                    // Key on first index
                    // Value on second index
                    // Then add to charMap
                    String[] charList = line.split(":");
                    Character character = charList[0].charAt(0);
                    int charScore = Integer.valueOf(charList[1]);
                    charMap.put(character, charScore);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println(language + "_charlist.txt wasn't found.");
        }
        return charMap;
    }

    private WordTrie generateWordTrie(String language) {
        long startTime = System.currentTimeMillis();
        InputStream is = getClass().getClassLoader().getResourceAsStream(language + "_wordlist.txt");
        WordTrie trie = new WordTrie();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF8"))) {
            for (String line; (line = br.readLine()) != null;) {
                if (!line.startsWith("#")) {
                    trie.addWord(line.toLowerCase());
                }
            }
        } catch (IOException ex) {
            System.out.println(language + "_wordlist.txt wasn't found.");
        } finally {
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println(language + "_trie generated in: " + totalTime + "ms.");
        }
        return trie;
    }

    public WordTrie get_en_trie() {
        return en_trie;
    }

    public WordTrie get_sv_trie() {
        return sv_trie;
    }

    public HashMap<Character, Integer> get_sv_charScores() {
        return sv_charScores;
    }

    public HashMap<Character, Integer> get_en_charScores() {
        return en_charScores;
    }

    public SessionManager getSessionManager() {
        return sm;
    }

    public ExecutorService getPool() {
        return pool;
    }
}
