package objects;

import java.util.Objects;
import org.json.JSONArray;

/**
 *
 * @author arouz
 */
public class Move implements Comparable<Move> {

    private final Word word;
    private int score;
    private final JSONArray words;
    private final JSONArray tiles;

    public Move(Word word) {
        this.word = word;
        this.words = new JSONArray();
        this.tiles = new JSONArray();
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

    public void addTile(int row, int column, String letter, boolean wildcard) {
        JSONArray tile = new JSONArray();
        tile.put(column);
        tile.put(row);
        tile.put(letter.toUpperCase());
        tile.put(wildcard);
        tiles.put(tile);
    }

    public void addWord(String word) {
        words.put(word.toUpperCase());
    }

    public JSONArray getTiles() {
        return tiles;
    }

    public JSONArray getWords() {
        return words;
    }

    @Override
    public String toString() {
        return "Move{score=" + score + ", word=" + word + ", tiles=" + getTiles().toString() + ", words=" + getWords().toString() + '}';
    }

    @Override
    public int compareTo(Move o) {
        int compareScore = o.getScore();
        return compareScore - getScore();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.word);
        hash = 89 * hash + this.score;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Move other = (Move) obj;
        if (this.getScore() != other.getScore()) {
            return false;
        }
        return getWord().getWord().equals(other.getWord().getWord());
    }

}
