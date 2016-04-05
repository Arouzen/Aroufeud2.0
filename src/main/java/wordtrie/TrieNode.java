package wordtrie;

/**
 *
 * @author arouz
 */
import java.util.ArrayList;
import java.util.List;
import objects.Word;

public class TrieNode {

    private TrieNode parent;
    private TrieNode[] children;
    private boolean isLeaf;     //Quick way to check if any children exist
    private boolean isWord;     //Does this node represent the last character of a word
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
     * child nodes for each successive letter in the word, in other words
     * recursive calls will be made with partial words.
     *
     * @param word the word to add
     */
    protected void addWord(String word) {
        isLeaf = false;

        int charPos;
        switch (word.charAt(0)) {
            // Hardcoded positions for swedish special characters
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
                // Regular english non-special characters node positions
                charPos = word.charAt(0) - 'a';
                break;
        }

        if (children[charPos] == null) {
            children[charPos] = new TrieNode(word.charAt(0));
            children[charPos].parent = this;
        }

        if (word.length() > 1) {
            children[charPos].addWord(word.substring(1));
        } else {
            children[charPos].isWord = true;
        }
    }

    /**
     * Returns the child TrieNode representing the given char, or null if no
     * node exists.
     *
     * @param c
     * @return
     */
    protected TrieNode getNode(char c) {
        int charPos;
        switch (c) {
            // Hardcoded positions for swedish special characters
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
                // Regular english non-special characters node positions
                charPos = c - 'a';
                break;
        }
        return children[charPos];
    }

    /**
     * Returns a List of String objects which are lower in the hierarchy than
     * this node with the characters available in the rack.
     *
     * @param rack
     * @param suffixLimit
     * @return
     */
    protected List<Word> getWords(ArrayList<String> rack, int suffixLimit) {
        //Create a list to return
        List<Word> list = new ArrayList<>();

        //If this node represents a word, add it to list as a Word object
        if (isWord) {
            // Also include the remaining rack
            list.add(new Word(toString(), rack));
        }

        //If current node has any children, recursive calls will be made to fetch the words that can be formed with our rack
        if (!isLeaf) {
            // Iterate the rack
            for (String rackChar : rack) {
                // Check if the current rackChar may form a word with this node as a parent
                if (getNode(rackChar.charAt(0)) != null) {
                    // Success!
                    // Create a temporary duplicate list of our rack
                    ArrayList<String> tmpRack = new ArrayList<>(rack);
                    // Remove our used character from the temporary rack
                    tmpRack.remove(rackChar);
                    // Keep going deeper in the recursive function with our temporary rack and with the limit decreased by 1
                    // Only go deeper if the suffixLimit hasn't reached 0 yet though.
                    if (suffixLimit > 0) {
                        list.addAll(getNode(rackChar.charAt(0)).getWords(tmpRack, suffixLimit - 1));
                    }
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

    // Return all children
    public ArrayList<TrieNode> getNodes() {
        ArrayList<TrieNode> nodes = new ArrayList<>();
        for (TrieNode child : children) {
            if (child != null) {
                nodes.add(child);
            }
        }
        return nodes;
    }

    public String getChar() {
        return String.valueOf(character);
    }

    public void setChar(char character) {
        this.character = character;
    }
}
