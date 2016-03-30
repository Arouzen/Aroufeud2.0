package objects;

/**
 *
 * @author arouz
 */
public class Move implements Comparable {

    private final Word word;
    private int score;
    private Tile starts;
    private Tile ends;

    public Move(Word word, int score, Tile starts, Tile ends) {
        this.word = word;
        this.score = score;
        this.starts = starts;
        this.ends = ends;
    }

    public Word getWord() {
        return word;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(Object cmp) {
        int compareScore = ((Move) cmp).getScore();
        return compareScore - getScore();
    }

    public Tile getStarts() {
        return starts;
    }

    public void setStarts(Tile starts) {
        this.starts = starts;
    }

    public Tile getEnds() {
        return ends;
    }

    public void setEnds(Tile ends) {
        this.ends = ends;
    }

    @Override
    public String toString() {
        return "Move{score=" + score + ", word=" + word + ", starts=" + starts + ", ends=" + ends + '}';
    }
}
