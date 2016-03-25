/*
 */
package managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.Game;
import objects.Tile;
import objects.Word;
import wordtrie.WordTrie;

/**
 *
 * @author arouz
 */
public class GameManager {

    public static void main(String[] args) {
        Game game = null;

        try {
            FileInputStream fin = new FileInputStream(new File("./troll.ser"));
            ObjectInputStream ois = new ObjectInputStream(fin);
            game = (Game) ois.readObject();
            ois.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //game.getBoard().printBoard();
        WordTrie trie = generateWordTrie("en");
        calculateMoves(game, trie);
    }

    private static void calculateMoves(Game game, WordTrie trie) {
        /*
        Iterate board
        Generate tasks
        Fetch valid moves
        Calculate score
        Add moves to sorted list
         */

        game.getBoard().printBoard();

        // Iterate board
        for (int row = 0; row < 15; row++) {
            for (int column = 0; column < 15; column++) {
                ArrayList<String> rack = game.getRack();

                // Find non empty tiles
                if (!game.getBoard().getTile(column, row).getLetter().equals("_")) {
                    // If there exists a non empty tile left of this tile, this tile is already a part of that tile's partialword
                    // Continue only if the tile left of this tile is empty and the tile is not on column position 0
                    if (column > 0 && game.getBoard().getTile(column - 1, row).getLetter().equals("_")) {
                        int limit = 0;
                        int tmpColumn = column;
                        // Check how many empty tiles there exists left of this tile. This number will be the limit. The limit can only be as high as tiles in the rack.

                        while (tmpColumn > 0 && limit <= rack.size()) {
                            tmpColumn--;
                            if (game.getBoard().getTile(tmpColumn, row).getLetter().equals("_")) {
                                limit++;
                            } else {
                                break;
                            }
                        }

                        // Find all letters after tile, namely the rightPartialWord
                        tmpColumn = column;
                        String rightPartialWord = "";
                        while (tmpColumn < 14) {
                            tmpColumn++;
                            if (!game.getBoard().getTile(tmpColumn, row).getLetter().equals("_")) {
                                rightPartialWord += game.getBoard().getTile(tmpColumn, row).getLetter();
                            } else {
                                break;
                            }
                        }

                        ArrayList<String> leftPartialWordMoves = calculateLeftPartialWords(rack, limit, game.getBoard().getTile(column, row), trie);
                        for (String leftPartialWord : leftPartialWordMoves) {
                            // Add everything in rightPartialWord + current tile to our rack
                            rack = game.getRack();
                            for (int i = 0; i < rightPartialWord.length(); i++) {
                                rack.add(String.valueOf(rightPartialWord.charAt(i)));
                            }
                            rack.add(game.getBoard().getTile(column, row).getLetter());

                            // Remove used letters from rack
                            String usedLetters = leftPartialWord + rightPartialWord;
                            for (int i = 0; i < usedLetters.length(); i++) {
                                rack.remove(String.valueOf(usedLetters.charAt(i)));
                            }

                            List<Word> words = trie.getWords(usedLetters, rack);
                            Collections.sort(words);
                            for (Word word : words) {
                                System.out.println(word);
                            }
                        }

                    }
                }
            }
        }

        game.getBoard().printBoard();
    }

    private static ArrayList<String> calculateLeftPartialWords(ArrayList<String> rack, int limit, Tile endTile, WordTrie trie) {
        return trie.leftPartialWords(rack, limit, endTile.getLetter().toLowerCase());
    }

    public void parseGameList(ArrayList<Game> gameList) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(new File("./troll.ser"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(fout);
        } catch (IOException ex) {
            Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            oos.writeObject(gameList.get(2));
        } catch (IOException ex) {
            Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static WordTrie generateWordTrie(String language) {
        long startTime = System.currentTimeMillis();
        File ordlista = new File(language + "_wordlist.txt");
        WordTrie trie = new WordTrie(language);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ordlista), "UTF8"))) {
            for (String line; (line = br.readLine()) != null;) {
                if (!line.startsWith("#")) {
                    trie.addWord(line.toLowerCase());
                }
            }
        } catch (IOException ex) {
            System.out.println("wordlist.txt wasn't found.");
        } finally {
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("Trie generated in: " + totalTime + "ms.");
        }
        /*List list = trie.getWords("aba");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }*/
        return trie;
    }
}
