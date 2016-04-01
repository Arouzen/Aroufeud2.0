package workers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
public class Worker implements Runnable {

    private final Game game;
    private final int curRow;
    private final ArrayList<String> rackWords;
    private final boolean rotated;
    private final Set<Move> horizontalMoves;
    private final WordTrie trie;
    private final HashMap<Character, Integer> charScores;
    private final ArrayList<String> rack;

    public Worker(Game game, int row, ArrayList<String> rackWords, boolean rotated, Set<Move> horizontalMoves, WordTrie trie, HashMap<Character, Integer> charScores, ArrayList<String> rack) {
        this.game = game;
        this.curRow = row;
        this.rackWords = new ArrayList<>(rackWords);
        this.rotated = rotated;
        this.horizontalMoves = horizontalMoves;
        this.trie = trie;//new WordTrie(trie);
        this.charScores = charScores;
        this.rack = rack;
    }

    @Override

    public void run() {
        try {
            _run();
        } catch (Exception e) {
            // Catch problems.
            e.printStackTrace();
        }
    }

    public void _run() {
        for (int column = 0; column < 15; column++) {
            // Start by checking if the tile is empty or not
            if (game.getBoard().getTile(curRow, column).getLetter().equals("_")) {

                // This tile is available. Check if any of our rackWords can be placed on this start position.
                // Dont hit any other letter, dont hit the right wall.
                for (String rackWord : rackWords) {
                    // Proceed if we don't hit right wall
                    if ((column + rackWord.length() - 1) < 15) {
                        // Check if we hit any already placed letter with this word
                        // Also check that the word connects with a already placed letter atleast once
                        int tmpColumn = column;
                        int i = 0;
                        boolean valid = true;
                        boolean connected = false;
                        while (tmpColumn < 14 && i < rackWord.length()) {
                            if (!game.getBoard().getTile(curRow, tmpColumn).getLetter().equals("_")) {
                                valid = false;
                                break;
                            }

                            // Check so we dont hit the top wall
                            if (curRow > 0 && !connected) {
                                // Check if this tile connects with a letter above it
                                if (!game.getBoard().getTile(curRow - 1, tmpColumn).getLetter().equals("_")) {
                                    connected = true;
                                }
                            }
                            if (curRow < 14 && !connected) {
                                // Check so we dont hit the bottom wall
                                // Check if this tile connects with a letter under it
                                if (!game.getBoard().getTile(curRow + 1, tmpColumn).getLetter().equals("_")) {
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
                            word.setAnchorTile(new Tile(curRow, column));
                            word.setAnchorPosition(0);
                            Move move = validateAndCalcMoveScore(word, game.getBoard(), charScores, trie, rotated);
                            if (move != null) {
                                if (word.getWord().length() == 7) {
                                    // If all tiles are used, 40 bonus points are awarded
                                    move.setScore(move.getScore() + 40);
                                }
                                horizontalMoves.add(move);
                            }
                        }
                    }
                }
                // Start by checking if this tile is already used before
                // Only proceed if the tile left of this anchor tile is empty or the column is zero, otherwise this anchor tile is already a part of that tile's every fullPrefix.

            } else if (column == 0 || game.getBoard().getTile(curRow, column - 1).getLetter().equals("_")) {
                // We found a anchor tile
                // Find all connected letters to the right of our anchor tile
                int tmpColumn = column;
                String lettersAfterAnchor = "";
                // Make sure we dont hit the right-side wall
                while (tmpColumn < 14) {
                    tmpColumn++;
                    if (!game.getBoard().getTile(curRow, tmpColumn).getLetter().equals("_")) {
                        lettersAfterAnchor += game.getBoard().getTile(curRow, tmpColumn).getLetter();
                    } else {
                        // If we hit empty tile, break loop
                        break;
                    }
                }

                // Count successive free tiles after the last character in letterAfterAnchor on the board
                int suffixLimit = 0;
                while (tmpColumn < 14) {
                    if (game.getBoard().getTile(curRow, tmpColumn).getLetter().equals("_")) {
                        suffixLimit++;
                        tmpColumn++;
                    } else {
                        break;
                    }
                }

                // Fetch all valid prefixes with our rack from the trie. Valid is defined as the prefix may form a word.
                ArrayList<String> prefixes = fetchValidPrefixesFromTrie(rack, game.getBoard().getTile(curRow, column), game.getBoard(), trie);

                // Iterate the prefixes
                for (String prefixWithRack : prefixes) {
                    // Extract the remaining rack from prefixWithRack
                    // Use -1 as limit on .split() to keep eventual empty list
                    String strRack = prefixWithRack.split(":", -1)[1];
                    ArrayList<String> tmpRack = new ArrayList<>();
                    for (char rackChar : strRack.toCharArray()) {
                        if (rackChar == '?') {
                            tmpRack.add("");
                        } else {
                            tmpRack.add(String.valueOf(rackChar));
                        }
                    }

                    String fullPrefix = prefixWithRack.split(":")[0] + lettersAfterAnchor;

                    // Fetch all valid words that can be generated with the full prefix and uses maximum of suffixLimit characters from the rack
                    List<Word> words = trie.getWords(fullPrefix, tmpRack, suffixLimit);

                    // Iterate the words
                    for (Word word : words) {
                        word.setAnchorTile(game.getBoard().getTile(curRow, column));
                        word.setAnchorPosition(prefixWithRack.split(":")[0].length() - 1);
                        // Calculate move score. Score can get very high if the move also creates words on the y axis with letters on the board.
                        // Also takes account of any multipliers on the board. 
                        // Returns null if a invalid word was formed on the board with this move.
                        Move move = validateAndCalcMoveScore(word, game.getBoard(), charScores, trie, rotated);
                        if (move != null) {
                            horizontalMoves.add(move);
                        }
                    }
                }
            }
        }
    }

    private ArrayList<String> fetchValidPrefixesFromTrie(ArrayList<String> rack, Tile anchorTile, Board board, WordTrie trie) {
        return trie.generateValidPrefixes(rack, anchorTile, board);
    }

    private Move validateAndCalcMoveScore(Word word, Board board, HashMap<Character, Integer> charScores, WordTrie trie, boolean rotated) {
        int moveScore = 0;
        Move move = new Move(word);

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
                // Prepend the character because we are moving to the left
                left = board.getTile(row, tmpColumn).getLetter() + left;
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

        // Proceed if the formed word is valid. If the move is invalid, return null.
        if (!trie.isWord(word.getWord())) {
            return null;
        }

        // Add word to move
        move.addWord(word.getWord());
        
        // Move column to first character of the full word
        column = column - left.length();

        // Calculate the score of this word. Add it to moveScore.
        moveScore += addScore(row, column, true, rotated, word.getWord(), charScores, board, rack);
        
        // Iterate all characters in the move we want to make to find if any Y axis words was formed if this character is placed on the board.
        // Also make sure that a move was made. If every character was already on the board, nothing was placed, thus the move is invalid.
        boolean moveMade = false;
        for (int i = 0; i < word.getWord().length(); i++) {
            // We only want to iterate what we are trying to place right now, not what was already placed before, so only proceed if this tile is empty on the board
            if (board.getTile(row, column).getLetter().equals("_")) {
                // Start with finding all connected letters above the character. Dont hit the upper wall.
                moveMade = true;
                int tmpRow = row;
                String above = "";
                while (tmpRow > 0) {
                    tmpRow--;
                    if (!board.getTile(tmpRow, column).getLetter().equals("_")) {
                        // Prepend the new letter because we are moving upwards
                        above = board.getTile(tmpRow, column).getLetter() + above;
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
                        // Append the character because we are moving downwards
                        below += board.getTile(tmpRow, column).getLetter();
                    } else {
                        break;
                    }
                }

                // If either above or below is not empty, we have formed a Y-axis word
                if ((above + below).length() > 0) {
                    // Validate and calculate the score of the Y-axis word
                    String yaxisWord;

                    // If we are in a rotated board or not though, is highly relevant how we see the connected yaxis-word
                    // Because when we rotate counterclockwise, the validation of any Y-axis words that may be formed is inverted.
                    // Words that gets formed from top -> bottom in the rotated board is actually words that gets formed from bottom -> top on the unrotated board
                    // 
                    // A word that was made C
                    //                      A
                    //                      T in the rotated view, is thus actually TAC, not CAT, when we rotate back clockwise.
                    // 
                    // When the board is rotated, we invert above and below, and form the word by adding the strings in the following order; below + char + above
                    // When the board is not rotated, we form the word by adding the strings in the following order; above + char + below
                    if (rotated) {
                        above = new StringBuilder(above).reverse().toString();
                        below = new StringBuilder(below).reverse().toString();
                        yaxisWord = below + word.getWord().charAt(i) + above;
                    } else {
                        yaxisWord = above + word.getWord().charAt(i) + below;
                    }

                    // Check if the formed word is valid. If no, return null. This move is invalid. 
                    if (!trie.isWord(yaxisWord)) {
                        return null;
                    }

                    // Add word to move
                    move.addWord(yaxisWord);

                    // A word was formed, and it's valid.
                    // Calculate the score of this word. Add it to moveScore.
                    moveScore += addScore(row + (rotated ? below.length() : -above.length()), column, false, rotated, yaxisWord, charScores, board, rack);
                }
            } else {
                // Make sure the letter on the board is the same as the character we tried to place. If not, invalid word, return null.
                String curLetter = board.getTile(row, column).getLetter();
                String curWordLetter = String.valueOf(word.getWord().charAt(i));
                if (!curLetter.equals(curWordLetter)) {
                    return null;
                }
            }
            // Keep iterating the remaining characters.
            column++;
        }

        // Proceed only if a move was made
        if (!moveMade) {
            return null;
        }

        /* Figure out the tiles we want to place, and add them to the move */
        // Fetch start and end location of the move. 
        // Move column to first character of the full word
        column = column - word.getWord().length();
        Tile startTile = new Tile(row, column);
        int wordIndex = 0;
        // Add the tiles we are trying to place to the move
        for (int col = startTile.getColumn(); col < startTile.getColumn() + word.getWord().length(); col++) {
            Tile curTile = board.getTile(startTile.getRow(), col);
            // Proceed if this col is empty on the board, in other words we are trying to place something here
            if (curTile.getLetter().equals("_")) {
                int _col = curTile.getColumn();
                int _row = curTile.getRow();
                if (rotated) {
                    _col = 14 - curTile.getRow();
                    _row = curTile.getColumn();
                }
                move.addTile(_row, _col, String.valueOf(word.getWord().charAt(wordIndex)),
                        !rack.contains(String.valueOf(word.getWord().charAt(wordIndex)))); // Check if the wordIndex char exists in the rack, if not this has to be a wildcard
            }
            wordIndex++;
        }
        // Move to the rightmost character
        column = column + word.getWord().length() - 1;
        Tile endTile;
        // Rotate the endTile back
        if (rotated) {
            endTile = new Tile(column, 14 - row);
        } else {
            endTile = new Tile(row, column);
        }
        move.setScore(moveScore);
        move.setStarts(startTile);
        move.setEnds(endTile);
        return move;
    }

    public int addScore(int row, int column, boolean horizontal, boolean rotated, String word, HashMap<Character, Integer> charScores, Board board, ArrayList<String> rack) {
        int multiplier = 1;
        int totScore = 0;
        Tile curTile;
        for (int i = 0; i < word.length(); i++) {
            if (horizontal) {
                curTile = board.getTile(row, column + i);
            } else {
                curTile = board.getTile(row + (rotated ? -i : i), column);
            }
            int charScore;

            // If this is a move we are trying to place, and the tile insn't in the rack. It's a wildcard, thus 0 points.
            if (curTile.getLetter().equals("_") && !rack.contains(String.valueOf(word.charAt(i)))) {
                charScore = 0;
            } else {
                charScore = charScores.get(word.charAt(i));
            }

            // Only deal with powers if the tile is empty (in other words ensure that the power is not already used)
            if (curTile.getLetter().equals("_") && curTile.getPower() != null) {
                switch (curTile.getPower()) {
                    case "DW":
                        multiplier *= 2;
                        break;
                    case "TW":
                        multiplier *= 3;
                        break;
                    case "DL":
                        charScore *= 2;
                        break;
                    case "TL":
                        charScore *= 3;
                        break;
                    default:
                        break;
                }
            }
            // Wildcards on the board always gives 0 score
            if (!curTile.isWildcard()) {
                totScore += charScore;
            }
        }
        return totScore * multiplier;
    }
}
