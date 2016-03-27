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
     * Fetch words in the Trie with the given prefix and a selection of
     * characters that can be used represented by a ArrayList of characters (a
     * rack)
     *
     * @param prefix
     * @param rack
     * @return a List with Word objects containing the string representation of
     * a word and its basic unmultiplied score from the Trie with the given
     * starting word prefix.
     */
    public List<Word> getWords(String prefix, ArrayList<String> rack) {
        // Find the node which represents the last letter of the prefix
        TrieNode lastNode = root;
        for (int i = 0; i < prefix.length(); i++) {
            lastNode = lastNode.getNode(prefix.toLowerCase().charAt(i));

            //If no node matches in our pursuit of the last node, then no words may be formed with this prefix, so return empty list. In other words: don't continue
            if (lastNode == null) {
                return new ArrayList<>();
            }
        }

        //Success! Return the words which is lower in the hierarchy from the last node in the prefix with our given rack
        return lastNode.getWords(rack);
    }

    /**
     * Generates valid prefixes that may form a word with our given rack (helper
     * function)
     *
     * @param rack
     * @param limit
     * @param endTile
     * @return
     */
    public ArrayList<String> generateValidPrefixes(ArrayList<String> rack, int limit, char endTile) {
        // Helper function. Start at root node, start with empty partWord, start with empty list to add prefixes to.
        return generateValidPrefixes(root, rack, "", limit, endTile);
    }

    /**
     * Generates valid prefixes that may form a word with our given rack
     * (recursive function) The prefix must be a maximum of limit long, and end
     * with the character represented by endChar.
     *
     * @param curNode
     * @param rack
     * @param curPrefix
     * @param endChar
     * @param prefixWords
     * @param limit
     * @return
     */
    public ArrayList<String> generateValidPrefixes(TrieNode curNode, ArrayList<String> rack, String curPrefix, int limit, char endChar) {
        ArrayList<String> prefixWords = new ArrayList<>();
        // Only continue iterating rack and going deeper into recursive function while limit is higher than zero
        if (limit > 0) {
            // Iteratate the rack
            for (String rackChar : rack) {
                // Clone our rack and remove our current rackChar from it
                ArrayList<String> tmpRack = (ArrayList<String>) rack.clone();
                tmpRack.remove(rackChar);
                // Check if partWord + rackTile may form more valid partialWords
                TrieNode nextNode = curNode.getNode(rackChar.charAt(0));
                if (nextNode != null) {
                    System.out.println(curNode.getNode(rackChar.charAt(0)).toString());
                    // Success! Go deeper! 
                    // Replace node with the node we just checked exists, add rackTile to partWord, decrease limit by 1, replace rack with tmpRack

                    // Check if nextNode has the endChar as a valid child, if yes; Valid prefix found and gets added to prefixWords
                    if (nextNode.getNode(endChar) != null) {
                        if (!prefixWords.contains(nextNode.toString())) {
                            prefixWords.add(nextNode.toString());
                        }
                    }
                    prefixWords.addAll(generateValidPrefixes(nextNode, tmpRack, curPrefix + rackChar, limit - 1, endChar));
                }
            }
        }
        return prefixWords;
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
            ex.printStackTrace();
            System.out.println(language + "_charlist.txt wasn't found.");
        }
        return charMap;
    }
}
