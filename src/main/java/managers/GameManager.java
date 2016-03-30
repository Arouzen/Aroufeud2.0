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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.Board;
import objects.Game;
import objects.Move;
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
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        //game.getBoard().printBoard();
        WordTrie trie = generateWordTrie("en");
        calculateMoves(game, trie);
    }

    private static void calculateMoves(Game game, WordTrie trie) {
        /*
        Iterate board
        Fetch valid words that can be formed on the board
        Validate the moves and calculate the score
        Add moves to sorted list
         */

        HashMap<Character, Integer> charScores = generateCharMap("en");
        ArrayList<Move> validMoves = new ArrayList<>();
        ArrayList<String> rack = (ArrayList<String>) game.getRack();

        
        /*
        game.getBoard().rotateBoard();
        game.getBoard().rotateBoard();
        game.getBoard().rotateBoard();
        game.getBoard().updatePowers();
*/
        
        game.getBoard().printBoard();

        // Start with finding all words that can be created with only the rack, without interference of the board
        ArrayList<String> rackWords = trie.generateWordsWithRack(rack);

        // Iterate all positions in board
        for (int row = 0; row < 15; row++) {
            for (int column = 0; column < 15; column++) {

                if (column == 7 && row == 7) {
                    int i = 1;
                }
                // Start by checking if the tile is empty or not
                if (game.getBoard().getTile(row, column).getLetter().equals("_")) {
                    // This tile is available. Check if any of our rackWords can be placed on this start position.
                    // Dont hit any other letter, dont hit the right wall.
                    for (String rackWord : rackWords) {
                        // Check if we hit right wall
                        if (column + rackWord.length() < 15) {
                            // Check if we hit any already placed letter with this word
                            // Also check that the word connects with a already placed letter atleast once
                            int tmpColumn = column;
                            int i = 0;
                            boolean valid = true;
                            boolean connected = false;
                            while (tmpColumn < 14 && i < rackWord.length()) {
                                if (!game.getBoard().getTile(row, tmpColumn).getLetter().equals("_")) {
                                    valid = false;
                                    break;
                                }

                                // Check so we dont hit the top wall
                                if (row > 0 && !connected) {
                                    // Check if this tile connects with a letter above it
                                    if (!game.getBoard().getTile(row - 1, tmpColumn).getLetter().equals("_")) {
                                        connected = true;
                                    }
                                }
                                if (row < 14 && !connected) {
                                    // Check so we dont hit the bottom wall
                                    // Check if this tile connects with a letter under it
                                    if (!game.getBoard().getTile(row + 1, tmpColumn).getLetter().equals("_")) {
                                        connected = true;
                                    }
                                }
                                i++;
                                tmpColumn++;
                            }
                            if (valid && connected) {
                                // Calculate move score. Score can get very high if the move also creates words on the y axis with letters on the board.
                                // Also takes account of any multipliers on the board. 
                                // Returns null if a invalid word was formed on the board with this move.
                                Word word = new Word(rackWord);
                                // Set anchor tile to the first character of the word
                                word.setAnchorTile(new Tile(row, column));
                                word.setAnchorPosition(0);
                                Move move = validateAndCalcMoveScore(word, game.getBoard(), charScores, trie);
                                if (move != null) {
                                    validMoves.add(move);
                                }
                            }
                        }
                    }
                } else // We found a anchor tile
                // Start by checking if this tile is already used before
                // If the tile left of this anchor tile is not empty, it means this anchor tile is already a part of that tile's every fullPrefix.
                {
                    if (column != 0 && game.getBoard().getTile(row, column - 1).getLetter().equals("_")) {
                        // Find all connected letters to the right of our anchor tile
                        int tmpColumn = column;
                        String lettersAfterAnchor = "";
                        // Make sure we dont hit the right-side wall
                        while (tmpColumn < 14) {
                            tmpColumn++;
                            if (!game.getBoard().getTile(row, tmpColumn).getLetter().equals("_")) {
                                lettersAfterAnchor += game.getBoard().getTile(row, tmpColumn).getLetter();
                            } else {
                                // If we hit empty tile, break loop
                                break;
                            }
                        }

                        // Fetch all valid prefixes with our rack from the trie. Valid is defined as the prefix may form a word.
                        ArrayList<String> prefixes = fetchValidPrefixesFromTrie(rack, game.getBoard().getTile(row, column), game.getBoard(), trie);

                        // Count successive free tiles after the last character in letterAfterAnchor on the board
                        int suffixLimit = 0;
                        while (tmpColumn < 14) {
                            if (game.getBoard().getTile(row, tmpColumn).getLetter().equals("_")) {
                                suffixLimit++;
                                tmpColumn++;
                            } else {
                                break;
                            }
                        }

                        // Iterate the prefixes
                        for (String prefixWithRack : prefixes) {
                            // Extract the remaining rack from prefixWithRack
                            // Use -1 as limit on .split() to keep eventual empty list
                            String strRack = prefixWithRack.split(":", -1)[1];
                            ArrayList<String> tmpRack = new ArrayList<>();
                            for (char rackChar : strRack.toCharArray()) {
                                tmpRack.add(String.valueOf(rackChar));
                            }

                            String fullPrefix = prefixWithRack.split(":")[0] + lettersAfterAnchor;

                            // Fetch all valid words that can be generated with the full prefix and uses maximum of suffixLimit characters from the rack
                            List<Word> words = trie.getWords(fullPrefix, tmpRack, suffixLimit);

                            // Iterate the words
                            for (Word word : words) {
                                word.setAnchorTile(game.getBoard().getTile(row, column));
                                word.setAnchorPosition(prefixWithRack.split(":")[0].length() - 1);
                                System.out.println(word.getWord());
                                // Calculate move score. Score can get very high if the move also creates words on the y axis with letters on the board.
                                // Also takes account of any multipliers on the board. 
                                // Returns null if a invalid word was formed on the board with this move.
                                Move move = validateAndCalcMoveScore(word, game.getBoard(), charScores, trie);
                                if (move != null) {
                                    // Capitalize the Nth character of the word (N = position of anchor tile) for print purposes
                                    String tmpWord = "";
                                    for (int i = 0; i < move.getWord().getWord().length(); i++) {
                                        if (i == move.getWord().getAnchorPosition()) {
                                            tmpWord += move.getWord().getWord().substring(i, i + 1).toUpperCase();
                                        } else {
                                            tmpWord += move.getWord().getWord().substring(i, i + 1);
                                        }
                                    }
                                    move.getWord().setWord(tmpWord);
                                    validMoves.add(move);
                                }
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(validMoves);
        for (Move move : validMoves) {
            System.out.println(move);
        }
        game.getBoard().printBoard();

    }

    private static ArrayList<String> fetchValidPrefixesFromTrie(ArrayList<String> rack, Tile anchorTile, Board board, WordTrie trie) {
        return trie.generateValidPrefixes(rack, anchorTile, board);
    }

    private static Move validateAndCalcMoveScore(Word word, Board board, HashMap<Character, Integer> charScores, WordTrie trie) {
        int moveScore = 0;
        // We want to move to the first position of the word, so we place ourselves on the anchor tile and subtract the column by the anchorPosition
        int column = word.getAnchorTile().getColumn();
        int row = word.getAnchorTile().getRow();

        column = column - word.getAnchorPosition();
        int tmpColumn = column;

        // Start by finding all characters left of our potential move. Dont hit the left wall.
        String left = "";
        while (tmpColumn > 0) {
            tmpColumn--;
            if (!board.getTile(row, tmpColumn).getLetter().equals("_")) {
                left += board.getTile(row, tmpColumn).getLetter();
            } else {
                break;
            }
        }

        // Move to the last character in our potential move. Increase column by the words's size - 1.
        tmpColumn = column + (word.getWord().length() - 1);

        // Find all characters right of our potential move. Dont hit the right wall.
        String right = "";
        while (tmpColumn < 14) {
            tmpColumn++;
            if (!board.getTile(row, tmpColumn).getLetter().equals("_")) {
                right += board.getTile(row, tmpColumn).getLetter();
            } else {
                break;
            }
        }

        // Update the word
        word.setWord(left + word.getWord() + right);

        // Check if the formed word is valid. If no, return null. This move is invalid.
        if (!trie.isWord(word.getWord())) {
            // TODO: figure out if this needs manual garbage handling
            word = null;
            return null;
        }

        // Calculate the score of this word. Add it to moveScore.
        moveScore += addScore(row, column - left.length(), true, word.getWord(), charScores, board);

        // Move column to first character of the full word
        column = column - left.length();

        // Iterate all characters in the move we want to make to find if any Y axis words was formed if this character is placed on the board.
        for (int i = 0; i < word.getWord().length(); i++) {
            // We only want to iterate what we are trying to place right now, not what was already placed before, so only proceed if this tile is empty on the board
            if (board.getTile(row, column).getLetter().equals("_")) {
                // Start with finding all connected letters above the character. Dont hit the upper wall.
                int tmpRow = row;
                String above = "";
                while (tmpRow > 0) {
                    tmpRow--;
                    if (!board.getTile(tmpRow, column).getLetter().equals("_")) {
                        above += board.getTile(tmpRow, column).getLetter();
                    } else {
                        break;
                    }
                }

                // Now find all connected letters below the character. Dont hit the lower wall.
                tmpRow = row;
                String below = "";
                while (tmpRow < 14) {
                    tmpRow++;
                    if (!board.getTile(tmpRow, column).getLetter().equals("_")) {
                        below += board.getTile(tmpRow, column).getLetter();
                    } else {
                        break;
                    }
                }

                // Proceed only if either below or above is not empty
                if ((below + above).length() > 0) {
                    // Check if the formed word is valid. If no, return null. This move is invalid. 
                    if (!trie.isWord(below + word.getWord().charAt(i) + above)) {
                        return null;
                    }
                    // A word was formed, and it's valid.
                    // Calculate the score of this word. Add it to moveScore.
                    moveScore += addScore(row - above.length(), column, false, below + word.getWord().charAt(i) + above, charScores, board);
                }
            }
            // Keep iterating the remaining characters.
            column++;
        }
        return new Move(word, moveScore);
    }

    public static int addScore(int row, int column, boolean horizontal, String word, HashMap<Character, Integer> charScores, Board board) {
        int multiplier = 1;
        int totScore = 0;
        Tile curTile;
        for (int i = 0; i < word.length(); i++) {
            if (horizontal) {
                curTile = board.getTile(row, column + i);
            } else {
                curTile = board.getTile(row + i, column);
            }
            int charScore = charScores.get(word.charAt(i));
            // Only deal with powers if the tile is empty (in other words ensure that the power is not already used)
            if (curTile.getLetter().equals("_") && curTile.getPower() != null) {
                if (curTile.getPower().equals("DW")) {
                    multiplier *= 2;
                } else if (curTile.getPower().equals("TW")) {
                    multiplier *= 3;
                } else if (curTile.getPower().equals("DL")) {
                    charScore *= 2;
                } else if (curTile.getPower().equals("TL")) {
                    charScore *= 3;
                }
            }
            totScore += charScore;
        }

        return totScore * multiplier;
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
            oos.writeObject(gameList.get(0));
        } catch (IOException ex) {
            Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generate HashMap with a character as key, and its score as value. Values
     * and keys given from external txt file.
     *
     * @return HashMap< Character,Integer >
     */
    private static HashMap<Character, Integer> generateCharMap(String language) {
        HashMap<Character, Integer> charMap = new HashMap<>();
        File charlist = new File(language + "_charlist.txt");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(charlist), "UTF8"))) {
            for (String line; (line = br.readLine()) != null;) {
                if (!line.startsWith("#")) {
                    // Split row on ":"
                    // Key on first index
                    // Value on second index
                    // Then add to charMap
                    String[] charList = line.split(":");
                    Character character = charList[0].charAt(0);
                    int charScore = Integer.valueOf(charList[1]);
                    charMap.put(character, charScore);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println(language + "_charlist.txt wasn't found.");
        }
        return charMap;
    }

    public static WordTrie generateWordTrie(String language) {
        long startTime = System.currentTimeMillis();
        File ordlista = new File(language + "_wordlist.txt");
        WordTrie trie = new WordTrie();
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
