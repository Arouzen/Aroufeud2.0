package wordtrie;

/**
 *
 * @author arouz
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WordTrie {

    private final TrieNode root;
    private final HashMap<Character, Integer> charScores;

    /**
     * Constructor
     */
    public WordTrie() {
        root = new TrieNode();
        charScores = generateCharMap();
    }

    /**
     * Adds a word to the Trie
     *
     * @param word
     */
    public void addWord(String word) {
        root.addWord(word.toLowerCase(), charScores);
    }

    /**
     * Get the words in the Trie with the given prefix
     *
     * @param prefix
     * @return a List containing String objects containing the words in the Trie
     * with the given prefix.
     */
    public List<String> getWords(String prefix) {
        //Find the node which represents the last letter of the prefix
        TrieNode lastNode = root;
        for (int i = 0; i < prefix.length(); i++) {
            lastNode = lastNode.getNode(prefix.charAt(i));

            //If no node matches, then no words exist, return empty list
            if (lastNode == null) {
                return new ArrayList<>();
            }
        }

        //Return the words which eminate from the last node
        return lastNode.getWords();
    }

    /**
     * Generate HashMap with a character as key, and its score as value. Values
     * and keys given from external txt file.
     *
     * @return HashMap< Character,Integer >
     */
    private HashMap<Character, Integer> generateCharMap() {
        HashMap<Character, Integer> charMap = new HashMap<>();
        File charlist = new File("charlist.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(charlist), "UTF8"))) {
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
            System.out.println("charlist.txt wasn't found.");
        }
        return charMap;
    }
}
