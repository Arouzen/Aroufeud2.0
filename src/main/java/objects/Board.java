/*
 */
package objects;

import java.util.ArrayList;
import org.json.JSONArray;

/**
 *
 * @author arouz
 */
public final class Board {

    ArrayList<ArrayList<Tile>> board;

    public Board(JSONArray tiles) {
        board = new ArrayList<>();
        // init 15x15 empty tiles
        for (int row = 0; row < 15; row++) {
            ArrayList<Tile> tmpRow = new ArrayList<>();
            for (int column = 0; column < 15; column++) {
                tmpRow.add(new Tile(row, column, "_", false));
            }
            board.add(tmpRow);
        }

        // Add played tiles to empty board
        for (Object tileJson : tiles) {
            JSONArray tile = (JSONArray) tileJson;
            Tile curTile = getTile(tile.getInt(1), tile.getInt(0));
            curTile.setLetter(tile.getString(2).toLowerCase());
            curTile.setWildcard(tile.getBoolean(3));
        }

        setPowers();
    }

    public Board(Board board) {
        ArrayList<ArrayList<Tile>> newBoard = new ArrayList<>();
        for (ArrayList<Tile> row : this.board) {
            ArrayList<Tile> newRow = new ArrayList<>();
            for (Tile newTile : row) {
                newRow.add(new Tile(newTile));
            }
            newBoard.add(newRow);
        }
        this.board = newBoard;
    }

    public void rotateCCWAndFlipBoard() {
        int m = board.size();

        ArrayList<ArrayList<Tile>> rotBoard = new ArrayList<>();
        // Fill rotBoard with 15x15 empty tiles
        for (int row = 0; row < 15; row++) {
            ArrayList<Tile> tmpRow = new ArrayList<>();
            for (int column = 0; column < 15; column++) {
                tmpRow.add(new Tile(row, column, "_", false));
            }
            rotBoard.add(tmpRow);
        }

        // Fill rotBoard with the values from board but rotated counterclockwise, and vertically flipped
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                Tile newTile = rotBoard.get(m - 1 - i).get(j);
                Tile oldTile = getTile(j, m - i - 1);

                newTile.setLetter(oldTile.getLetter());
                newTile.setPower(oldTile.getPower());
                newTile.setWildcard(oldTile.isWildcard());
            }
        }

        board = rotBoard;
    }

    public void printBoard() {
        for (int row = 0; row < 15; row++) {
            for (int column = 0; column < 15; column++) {
                Tile currTile = getTile(row, column);
                System.out.print(currTile.getLetter());
            }
            System.out.println("");
        }
    }

    public Tile getTile(int row, int column) {
        return this.board.get(row).get(column);
    }

    public void setPowers() {
        ArrayList<Tile> TLs = new ArrayList<>();
        ArrayList<Tile> TWs = new ArrayList<>();
        ArrayList<Tile> DLs = new ArrayList<>();
        ArrayList<Tile> DWs = new ArrayList<>();

        // Flag tiles for bonus multipliers
        TLs.add(new Tile(0, 0));
        TLs.add(new Tile(0, 14));
        TLs.add(new Tile(1, 5));
        TLs.add(new Tile(1, 9));
        TLs.add(new Tile(3, 3));
        TLs.add(new Tile(3, 11));
        TLs.add(new Tile(5, 1));
        TLs.add(new Tile(5, 5));
        TLs.add(new Tile(5, 9));
        TLs.add(new Tile(5, 13));
        TLs.add(new Tile(9, 1));
        TLs.add(new Tile(9, 5));
        TLs.add(new Tile(9, 9));
        TLs.add(new Tile(9, 13));
        TLs.add(new Tile(11, 3));
        TLs.add(new Tile(11, 11));
        TLs.add(new Tile(13, 5));
        TLs.add(new Tile(13, 9));
        TLs.add(new Tile(14, 0));
        TLs.add(new Tile(14, 14));

        TWs.add(new Tile(0, 4));
        TWs.add(new Tile(0, 10));
        TWs.add(new Tile(4, 0));
        TWs.add(new Tile(4, 14));
        TWs.add(new Tile(10, 0));
        TWs.add(new Tile(10, 14));
        TWs.add(new Tile(14, 4));
        TWs.add(new Tile(14, 10));

        DLs.add(new Tile(0, 7));
        DLs.add(new Tile(1, 1));
        DLs.add(new Tile(1, 13));
        DLs.add(new Tile(2, 6));
        DLs.add(new Tile(2, 8));
        DLs.add(new Tile(4, 6));
        DLs.add(new Tile(4, 8));
        DLs.add(new Tile(6, 2));
        DLs.add(new Tile(6, 4));
        DLs.add(new Tile(6, 10));
        DLs.add(new Tile(6, 12));
        DLs.add(new Tile(7, 0));
        DLs.add(new Tile(7, 14));
        DLs.add(new Tile(8, 2));
        DLs.add(new Tile(8, 4));
        DLs.add(new Tile(8, 10));
        DLs.add(new Tile(8, 12));
        DLs.add(new Tile(10, 6));
        DLs.add(new Tile(10, 8));
        DLs.add(new Tile(12, 6));
        DLs.add(new Tile(12, 8));
        DLs.add(new Tile(13, 1));
        DLs.add(new Tile(13, 13));
        DLs.add(new Tile(14, 7));

        DWs.add(new Tile(2, 2));
        DWs.add(new Tile(2, 12));
        DWs.add(new Tile(3, 7));
        DWs.add(new Tile(4, 4));
        DWs.add(new Tile(4, 10));
        DWs.add(new Tile(7, 3));
        DWs.add(new Tile(7, 11));
        DWs.add(new Tile(10, 4));
        DWs.add(new Tile(10, 10));
        DWs.add(new Tile(11, 7));
        DWs.add(new Tile(12, 2));
        DWs.add(new Tile(12, 12));

        for (ArrayList<Tile> row : board) {
            for (Tile curTile : row) {
                // Update tile powers
                if (DWs.contains(curTile)) {
                    curTile.setPower("DW");
                } else if (TWs.contains(curTile)) {
                    curTile.setPower("TW");
                } else if (DLs.contains(curTile)) {
                    curTile.setPower("DL");
                } else if (TLs.contains(curTile)) {
                    curTile.setPower("TL");
                }
            }
        }
    }
}
