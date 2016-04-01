/*
 */
package objects;

import java.util.ArrayList;

/**
 *
 * @author arouz
 */
public class Word {

    private Tile anchorTile;
    private int anchorPosition;
    private String word;
    private ArrayList<String> rack;

    public Word(String word) {
        this.word = word;
        this.rack = new ArrayList<>();
    }
    
    public Word(String word, ArrayList<String> rack) {
        this.word = word;
        this.rack = rack;
    }

    public Word(Word word) {
        this.word = word.getWord();
        this.anchorTile = new Tile(word.getAnchorTile());
        this.anchorPosition = word.getAnchorPosition();
        this.rack = word.getRack();
    }

    public int getAnchorPosition() {
        return anchorPosition;
    }

    public void setAnchorPosition(int anchorPosition) {
        this.anchorPosition = anchorPosition;
    }

    public void setAnchorTile(Tile anchorTile) {
        this.anchorTile = anchorTile;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public ArrayList<String> getRack() {
        return rack;
    }

    @Override
    public String toString() {
        return "Word{word=" + word + ", anchorTile=" + anchorTile + ", anchorPos=" + anchorPosition + "'}'";
    }

    public Tile getAnchorTile() {
        return anchorTile;
    }

}
