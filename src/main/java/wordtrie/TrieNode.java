package wordtrie;

/**
 *
 * @author arouz
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import objects.Word;

public class TrieNode {

    private TrieNode parent;
    private TrieNode[] children;
    private boolean isLeaf;     //Quick way to check if any children exist
    private boolean isWord;     //Does this node represent the last character of a word
    private int wordScore;      //Total word score excluding score generated by bonus tiles
    private char character;     //The character this node represents

    /**
     * Constructor for the top level root node.
     */
    public TrieNode() {
        children = new TrieNode[29];
        isLeaf = true;
        isWord = false;
    }

    /**
     * Constructor for child nodes.
     *
     * @param character
     */
    public TrieNode(char character) {
        this();
        this.character = character;
    }

    /**
     * Returns wordScore of a word if the current node represents a valid word,
     * else return -1
     *
     * @return
     */
    public int getWordScore() {
        if (isWord) {
            return wordScore;
        } else {
            return -1;
        }
    }

    /**
     * Returns True if the current node represents the last character of a valid
     * word
     *
     * @return
     */
    public boolean isWord() {
        return isWord;
    }

    /**
     * Adds a word to this node. This method is called recursively and adds
     * child nodes for each successive letter in the word, therefore recursive
     * calls will be made with partial words.
     *
     * @param word the word to add
     * @param totScore word score
     * @param charScores hashmap with character scores
     */
    protected void addWord(String word, int totScore, HashMap<Character, Integer> charScores) {
        isLeaf = false;

        
        // Swedish character support
        int charPos;
        switch (word.charAt(0)) {
            case 'å':
                charPos = 26;
                break;
            case 'ä':
                charPos = 27;
                break;
            case 'ö':
                charPos = 28;
                break;
            default:
                charPos = word.charAt(0) - 'a';
                break;
        }

        if (children[charPos] == null) {
            children[charPos] = new TrieNode(word.charAt(0));
            children[charPos].parent = this;
        }

        totScore += charScores.get(word.charAt(0));

        if (word.length() > 1) {
            children[charPos].addWord(word.substring(1), totScore, charScores);
        } else {
            children[charPos].isWord = true;
            children[charPos].wordScore = totScore;
        }
    }

    /**
     * Helper function for addWord(String word)
     *
     * @param word
     * @param charScores
     */
    protected void addWord(String word, HashMap<Character, Integer> charScores) {
        // Start recursive function with totScore set to zero
        addWord(word, 0, charScores);
    }

    /**
     * Returns the child TrieNode representing the given char, or null if no
     * node exists.
     *
     * @param c
     * @return
     */
    protected TrieNode getNode(char c) {
        return children[c - 'a'];
    }

    /**
     * Returns a List of String objects which are lower in the hierarchy that
     * this node.
     *
     * @return
     */
    protected List<Word> getWords(ArrayList<String> rack) {
        //Create a list to return
        List<Word> list = new ArrayList();

        //If this node represents a word, add it to list as score:word
        if (isWord) {
            list.add(new Word(toString(), wordScore));
        }

        //If any children, recursive call
        if (!isLeaf) {
            //Add any words belonging to any children
            for (TrieNode child : children) {
                if (child != null && rack.contains(String.valueOf(child.character))) {
                    ArrayList<String> tmpRack = rack;
                    tmpRack.remove(String.valueOf(child.character));
                    list.addAll(child.getWords(tmpRack));
                }
            }
        }
        return list;
    }

    /**
     *
     * Recursive function, get all parent nodes characters and this nodes
     * character to form a string
     *
     * @return String this node is representing
     */
    @Override
    public String toString() {
        if (parent == null) {
            return "";
        } else {
            return parent.toString() + new String(new char[]{character});
        }
    }
}
