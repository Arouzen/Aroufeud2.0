/*
 */
package objects;

/**
 *
 * @author arouz
 */
public class Word {

    private Tile anchorTile;
    private int anchorPosition;
    private String word;

    public Word(String word) {
        this.word = word;
    }

    public Word(Word word) {
        this.word = word.getWord();
        this.anchorTile = new Tile(word.getAnchorTile());
        this.anchorPosition = word.getAnchorPosition();
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

    @Override
    public String toString() {
        return "Word{word=" + word + ", anchorTile=" + anchorTile + ", anchorPos=" + anchorPosition + "'}'";
    }

    public Tile getAnchorTile() {
        return anchorTile;
    }

}
