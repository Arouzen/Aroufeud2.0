/*
 */
package objects;

import java.io.Serializable;

/**
 *
 * @author arouz
 */
public class Tile implements Serializable {

    private int x;
    private int y;
    private String letter;
    private boolean TW;

    public Tile(int x, int y, String letter, boolean TW) {
        this.x = x;
        this.y = y;
        this.letter = letter;
        this.TW = TW;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getLetter() {
        return letter;
    }

    public boolean isTW() {
        return TW;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public void setTW(boolean TW) {
        this.TW = TW;
    }

    @Override
    public String toString() {
        return "Tile{" + "x=" + x + ", y=" + y + ", letter=" + letter + ", TW=" + TW + '}';
    }
}
