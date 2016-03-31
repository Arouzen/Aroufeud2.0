package objects;

import org.json.JSONArray;

/**
 *
 * @author arouz
 */
public class Move implements Comparable {

    private final Word word;
    private int score;
    private Tile starts;
    private Tile ends;
    private JSONArray words;
    private JSONArray tiles;

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
    
    public void addTile(int row, int column, String letter) {
        JSONArray tile = new JSONArray();
        tile.put(column);
        tile.put(row);
        tile.put(letter.toUpperCase());
        tile.put(false);
        tiles.put(tile);
    }
    
    public void addWord(String word) {
        words.put(word.toUpperCase());
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
    
    public JSONArray getTiles() {
        return tiles;
    }
    
    public JSONArray getWords() {
        return words;
    }

    @Override
    public String toString() {
        return "Move{score=" + score + ", word=" + word + ", starts=" + starts + ", ends=" + ends + '}';
    }
}
