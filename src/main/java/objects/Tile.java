package objects;

import java.io.Serializable;

/**
 *
 * @author arouz
 */
public class Tile implements Serializable {

    private final int column;
    private final int row;
    private String power;
    private String letter;

    public Tile(int row, int column, String letter) {
        this.column = column;
        this.row = row;
        this.letter = letter;
    }

    public Tile(Tile anchorTile) {
        this.column = anchorTile.getColumn();
        this.row = anchorTile.getRow();
        this.letter = anchorTile.getLetter();
        this.power = anchorTile.getPower();
    }

    public Tile(int row, int column) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public String getLetter() {
        return letter;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    @Override
    public String toString() {
        if (letter != null && power != null) {
            return "Tile{row=" + row + ", column=" + column + ", letter=" + letter + ", power=" + power + '}';
        } else {
            return "Tile{row=" + row + ", column=" + column + '}';
        }
    }

    // Generated by NetBeans
    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    // Generated by NetBeans
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
        final Tile other = (Tile) obj;
        if (this.column != other.column) {
            return false;
        }
        return this.row == other.row;
    }
}
