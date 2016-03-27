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
                ArrayList<String> rack = (ArrayList<String>) game.getRack();

                // Find non empty tiles
                if (!game.getBoard().getTile(column, row).getLetter().equals("_")) {
                    // Count empty tiles left of our anchor tile, this will be the limit of our prefix
                    int limit = 0;
                    boolean alreadyUsed = false;
                    // Check if there is a wall left of our anchor tile, if yes then limit has to be 0. No prefix!
                    if (column != 0) {
                        // If there exists a non empty tile left of this tile, this tile is already a part of that tile's partialword. SKIP THIS ANCHOR TILE!
                        if (!game.getBoard().getTile(column - 1, row).getLetter().equals("_")) {
                            // This tile is already a part of another already calculated move! Flagged!
                            alreadyUsed = true;
                        } else {
                            // Keep moving left (decreasing tmpColumn by one) until we hit a non empty tile or the wall
                            // After each successful move, increase limit by one
                            // The limit can only be as high as tiles in the rack.
                            int tmpColumn = column;
                            while (tmpColumn > 0 && limit < rack.size()) {
                                tmpColumn--;
                                if (game.getBoard().getTile(tmpColumn, row).getLetter().equals("_")) {
                                    limit++;
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    // Check our flag. Continue if not flagged.
                    if (!alreadyUsed) {
                        // Find all letters after our anchor tile
                        int tmpColumn = column;
                        String anchorLetter = game.getBoard().getTile(tmpColumn, row).getLetter();
                        String lettersAfterAnchor = "";
                        // Make sure we dont hit the right-side wall
                        while (tmpColumn < 14) {
                            tmpColumn++;
                            if (!game.getBoard().getTile(tmpColumn, row).getLetter().equals("_")) {
                                lettersAfterAnchor += game.getBoard().getTile(tmpColumn, row).getLetter();
                            } else {
                                // If we hit empty tile, break loop
                                break;
                            }
                        }

                        ArrayList<String> prefixes = fetchValidPrefixesFromTrie(rack, limit, game.getBoard().getTile(column, row), trie);
                        for (String prefix : prefixes) {
                            // Add everything in rightPartialWord + current tile to our rack
                            rack = game.getRack();
                            for (int i = 0; i < lettersAfterAnchor.length(); i++) {
                                rack.add(String.valueOf(lettersAfterAnchor.charAt(i)));
                            }
                            rack.add(anchorLetter);

                            // Remove used letters from rack
                            String fullPrefix = prefix + anchorLetter + lettersAfterAnchor;
                            for (int i = 0; i < fullPrefix.length(); i++) {
                                rack.remove(String.valueOf(fullPrefix.charAt(i)));
                            }

                            List<Word> words = trie.getWords(fullPrefix, rack);
                            Collections.sort(words);
                            for (Word word : words) {
                                word.setAnchorTile(game.getBoard().getTile(column, row));
                                word.setLimit(prefix.length());
                                // Capitalize Nth character in word (meaning capitalize our anchor tile)
                                String tmpWord = "";
                                for (int i = 0; i < word.getWord().length(); i++) {
                                    if (i == word.getLimit()) {
                                        tmpWord += word.getWord().substring(i, i + 1).toUpperCase();
                                    } else {
                                        tmpWord += word.getWord().substring(i, i + 1);
                                    }
                                }
                                word.setWord(tmpWord);
                                System.out.println(word);
                            }
                        }
                    }
                }
            }
        }

        game.getBoard().printBoard();
    }

    private static ArrayList<String> fetchValidPrefixesFromTrie(ArrayList<String> rack, int limit, Tile endTile, WordTrie trie) {
        return trie.generateValidPrefixes(rack, limit, endTile.getLetter().toLowerCase().charAt(0));
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
