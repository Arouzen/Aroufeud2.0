package wordtrie;

/**
 *
 * @author arouz
 */
import java.util.ArrayList;
import java.util.List;
import objects.Board;
import objects.Tile;
import objects.Word;

public class WordTrie {

    private final TrieNode root;

    /**
     * Constructor
     *
     */
    public WordTrie() {
        root = new TrieNode();
    }

    /**
     * Adds a word to the Trie
     *
     * @param word
     */
    public void addWord(String word) {
        root.addWord(word.toLowerCase());
    }

    /**
     * Is the word given a valid word in the trie?
     *
     * @param word
     * @return
     */
    public boolean isWord(String word) {
        // Start at root node
        TrieNode lastNode = root;
        // Iterate all characters in the word
        for (int i = 0; i < word.length(); i++) {
            // Go deeper in the trie
            lastNode = lastNode.getNode(word.charAt(i));

            // If a node returns null, the word is thus not a valid one. 
            if (lastNode == null) {
                return false;
            }
        }
        // We reached the last node! Is this a full word or a partial one though?
        return lastNode.isWord();
    }

    /**
     * Fetch words in the Trie with the given prefix and a selection of
     * characters that can be used represented by a ArrayList of characters (a
     * rack)
     *
     * @param prefix
     * @param rack
     * @param suffixLimit
     * @return a List with Word objects containing the string representation of
     * a word and its basic unmultiplied score from the Trie with the given
     * starting word prefix.
     */
    public List<Word> getWords(String prefix, ArrayList<String> rack, int suffixLimit) {
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
        return lastNode.getWords(rack, suffixLimit);
    }

    /**
     * Fetches all valid words with our given rack (helper function)
     *
     * @param rack
     * @return
     */
    public ArrayList<String> generateWordsWithRack(ArrayList<String> rack) {
        // Helper function. Start at root node, start with empty partWord.
        return generateWordsWithRack(root, rack, "");
    }

    /**
     * Generates valid prefixes that may form a word with our given rack
     * (recursive function) The prefix must be a maximum of limit long, and end
     * with the character represented by endChar.
     *
     * @param curNode
     * @param rack
     * @param curPrefix
     * @return
     */
    public ArrayList<String> generateWordsWithRack(TrieNode curNode, ArrayList<String> rack, String curPrefix) {
        ArrayList<String> validWords = new ArrayList<>();
        // Iteratate the rack
        for (String rackChar : rack) {
            // Clone our rack and remove our current rackChar from it
            ArrayList<String> tmpRack = (ArrayList<String>) rack.clone();
            tmpRack.remove(rackChar);
            // Check if partWord + rackTile may form more valid partialWords
            TrieNode nextNode = curNode.getNode(rackChar.charAt(0));
            if (nextNode != null) {
                // Success! Go deeper! 
                // Replace node with the node we just checked exists, add rackTile to partWord, replace rack with tmpRack
                // If nextNode is a valid word, add it to validWords
                if (nextNode.isWord()) {
                    if (!validWords.contains(nextNode.toString())) {
                        validWords.add(nextNode.toString());
                    }
                }
                validWords.addAll(generateWordsWithRack(nextNode, tmpRack, curPrefix + rackChar));
            }
        }
        return validWords;
    }

    /**
     * Generates valid prefixes that may form a word with our given rack (helper
     * function)
     *
     * @param rack
     * @param anchorTile
     * @param board
     * @return
     */
    public ArrayList<String> generateValidPrefixes(ArrayList<String> rack, Tile anchorTile, Board board) {
        ArrayList<String> prefixWords = new ArrayList<>();
        // For every column 0 to anchorTile.column, generate prefixes with this column as starter tile
        for (int column = 0; column < anchorTile.getColumn() + 1; column++) {
            // Check left of tile. Don't check if column is zero though, as that would lead to collision with left wall.
            // If the tile is connected with a non empty tile to the left, skip it. Invalid starter tile.
            if (column == 0 || board.getTile(anchorTile.getRow(), column - 1).getLetter().equals("_")) {
                // Generate prefixes with this tile as starter tile, add results to prefixWords
                ArrayList<String> validPrefixes = generateValidPrefixes(root, rack, column, "", anchorTile, board);
                prefixWords.addAll(validPrefixes);
            }
        }
        return prefixWords;
    }

    /**
     * Generates valid prefixes that may form a word with our given rack
     * (recursive function) The prefix must be a maximum of limit long, and end
     * with the character represented by endChar.
     *
     * @param curNode
     * @param rack
     * @param column
     * @param curPrefix
     * @param anchorTile
     * @param board
     * @return ArrayList of String representation of "validPrefix:remainingRack"
     */
    public ArrayList<String> generateValidPrefixes(TrieNode curNode, ArrayList<String> rack, int column, String curPrefix, Tile anchorTile, Board board) {
        ArrayList<String> prefixWords = new ArrayList<>();
        TrieNode nextNode;
        // Get the tile (anchorTile.row, column) from the board
        Tile curTile = board.getTile(anchorTile.getRow(), column);
        // Check if this tile is empty or not
        if (!curTile.getLetter().equals("_")) {
            // Proceed if the current node may form a prefix with this letter as the next node
            nextNode = curNode.getNode(curTile.getLetter().charAt(0));
            if (nextNode != null) {
                // Check if we are at the anchorTile
                if (column == anchorTile.getColumn()) {
                    // Also add the remainder of our rack after a delimeter to the prefix
                    String joinedRack = "";
                    for (String rackChar : rack) {
                        joinedRack += rackChar;
                    }
                    prefixWords.add(curPrefix + anchorTile.getLetter() + ":" + joinedRack);
                } else {
                    // Go deeper. Replace curNode with nextNode, append curTile's letter to curPrefix and move column to the right by adding column by 1
                    prefixWords.addAll(generateValidPrefixes(nextNode, rack, column + 1, curPrefix + curTile.getLetter(), anchorTile, board));
                }
            }
        } else {
            // The tile is empty. Iterate the rack for possible move scenarios
            for (String rackChar : rack) {
                // Clone the rack, and remove the current rackChar from it
                ArrayList<String> tmpRack = (ArrayList<String>) rack.clone();
                tmpRack.remove(rackChar);
                // Proceed if current node may form a prefix with the rackChar as the next node
                nextNode = curNode.getNode(rackChar.charAt(0));
                if (nextNode != null) {
                    // Go deeper! Replace curNode with nextNode, replace rack with tmpRack, append rackChar to curPrefix and move column to the right by adding column by 1
                    prefixWords.addAll(generateValidPrefixes(nextNode, tmpRack, column + 1, curPrefix + rackChar, anchorTile, board));
                }
            }
        }
        return prefixWords;
    }
}
