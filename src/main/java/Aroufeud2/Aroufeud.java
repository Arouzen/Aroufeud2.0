package Aroufeud2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import net.arouz.aroufeud.SessionManager;
import net.arouz.aroufeud.WordTrie;
import org.json.JSONObject;

/**
 *
 * @author arouz
 */
public class Aroufeud {

    public static void main(String[] args) {
        SessionManager sm = new SessionManager();
        String email = "lightpepsii@gmail.com";
        String password = "bajsapa123";
                
        boolean success = doLogin(email, password, sm);
        if (success) {
            System.out.println("Login successful!");
        }
    }

    private static boolean doLogin(String email, String password, SessionManager sm) {
        JSONObject jsonObj = null;
        try {
            jsonObj = sm.doLogin("lightpepsii@gmail.com", "bajsapa123");
            System.out.println(jsonObj.toString());
            if (jsonObj.get("status").equals("success")) {
                return true;
            } else {
                System.out.println(((JSONObject)jsonObj.get("content")).get("type"));
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
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
