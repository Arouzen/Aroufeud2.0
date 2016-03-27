/*
 */
package objects;

/**
 *
 * @author arouz
 */
public class Word implements Comparable {

    private Tile anchorTile;
    private int limit;
    private String word;
    private int score;

    public Word(String word, int score) {
        this.word = word;
        this.score = score;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(Object cmp) {
        int compareScore = ((Word) cmp).getScore();
        return compareScore - getScore();
    }

    @Override
    public String toString() {
        return "Word{word=" + word + ", score=" + score + ", anchorTile=" + anchorTile + ", limit=" + limit + "'}'";
    }

    public Tile getAnchorTile() {
        return anchorTile;
    }

}
