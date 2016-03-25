/*
 */
package objects;

import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONArray;

/**
 *
 * @author arouz
 */
public class Board implements Serializable {

    ArrayList<ArrayList<Tile>> board;

    public Board(JSONArray tiles) {
        board = new ArrayList<>();
        // init 15x15 empty tiles
        for (int i = 0; i < 15; i++) {
            ArrayList<Tile> row = new ArrayList<>();
            for (int ii = 0; ii < 15; ii++) {
                row.add(new Tile(i, ii, "_", false));
            }
            board.add(row);
        }
        
        // Add played tiles to empty board
        for (Object tileJson : tiles) {
            JSONArray tile = (JSONArray) tileJson;
            /*
            X,
            Y,
            LETTER,
            BOOLEAN (TW??)
            */
            Tile currTile = board.get(tile.getInt(0)).get(tile.getInt(1));
            currTile.setLetter(tile.getString(2));
            currTile.setTW(tile.getBoolean(3));
        }
    }

    public void printBoard() {
        for (int i = 0; i < 15; i++) {
            for (int ii = 0; ii < 15; ii++) {
                Tile currTile = board.get(ii).get(i);
                System.out.print(currTile.getLetter());
            }
            System.out.println("");
        }
    }
    
    public ArrayList<ArrayList<Tile>> getBoard() {
        return this.board;
    }

    public Tile getTile(int column, int row) {
        return this.board.get(column).get(row);
    }
}
