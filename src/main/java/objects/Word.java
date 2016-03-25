/*
 */
package objects;

import java.util.ArrayList;

/**
 *
 * @author arouz
 */
public class Word implements Comparable {

    ArrayList<Tile> placedTiles;
    String word;
    int score;

    public Word(String word, int score) {
        this.word = word;
        this.score = score;
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
        return "Word{" + "word=" + word + ", score=" + score + '}';
    }

}
