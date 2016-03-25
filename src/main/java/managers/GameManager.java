/*
 */
package managers;

import java.util.ArrayList;
import objects.Game;

/**
 *
 * @author arouz
 */
public class GameManager {

    /*public static void main(String[] args) {
        ArrayList<Game> gameList = new ArrayList<>();
        
    }*/
    
    public void parseGameList(ArrayList<Game> gameList) {

        for (Game game : gameList) {
            game.getBoard().printBoard();
        }
    }

}
