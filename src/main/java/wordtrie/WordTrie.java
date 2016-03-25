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
import objects.Word;

public class WordTrie {

    private final TrieNode root;
    private final HashMap<Character, Integer> charScores;

    /**
     * Constructor
     *
     * @param language
     */
    public WordTrie(String language) {
        root = new TrieNode();
        charScores = generateCharMap(language);
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
     * @return a List containing Word objects containing the words and scores in
     * the Trie with the given left-partial-word prefix.
     */
    public List<Word> getWords(String prefix, ArrayList<String> rack) {
        //Find the node which represents the last letter of the prefix
        TrieNode lastNode = root;
        for (int i = 0; i < prefix.length(); i++) {
            lastNode = lastNode.getNode(prefix.toLowerCase().charAt(i));

            //If no node matches, then no words exist, return empty list
            if (lastNode == null) {
                return new ArrayList<>();
            }
        }

        //Return the words which eminate from the last node
        return lastNode.getWords(rack);
    }

    public ArrayList<String> leftPartialWords(ArrayList<String> rack, int limit, String endTile) {
        // Helper function. Start at root node, start with empty partWord
        return leftPartialWords(root, rack, "", limit, endTile);
    }

    public ArrayList<String> leftPartialWords(TrieNode node, ArrayList<String> rack, String partWord, int limit, String endTile) {
        // Create copy of rack
        ArrayList<String> tmpRack;
        // Create return list
        ArrayList<String> partialWords = new ArrayList<>();

        if (limit == 0) {
            // Reached limit. partWord will be our left partial word only if the partWord + our tile returns a node that is not null
            if (node.getNode(endTile.charAt(0)) != null) {
                partialWords.add(partWord + endTile);
            }
        } else {
            // Limit not yet reached, keep iterating the rack
            for (String rackTile : rack) {
                tmpRack = (ArrayList<String>) rack.clone();
                tmpRack.remove(rackTile);
                // Check if partWord + rackTile will form more valid partialWords
                TrieNode nextNode = node.getNode(rackTile.toLowerCase().charAt(0));
                if (nextNode != null) {
                    // Go deeper! Replace node with the node we just checked exists, add rackTile to partWord, decrease limit by 1, replace rack with tmpRack
                    partialWords.addAll(leftPartialWords(nextNode, tmpRack, partWord+rackTile, limit - 1, endTile));
                }
            }
        }
        return partialWords;
    }

    /**
     * Generate HashMap with a character as key, and its score as value. Values
     * and keys given from external txt file.
     *
     * @return HashMap< Character,Integer >
     */
    private HashMap<Character, Integer> generateCharMap(String language) {
        HashMap<Character, Integer> charMap = new HashMap<>();
        File charlist = new File(language + "_charlist.txt");
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
