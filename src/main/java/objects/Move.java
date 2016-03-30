package objects;

/**
 *
 * @author arouz
 */
public class Move implements Comparable {

    private final Word word;
    private int score;

    public Move(Word word, int score) {
        this.word = word;
        this.score = score;
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

    @Override
    public String toString() {
        return "Move{score=" + score + ", word=" + word + '}';
    }
}
