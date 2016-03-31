package managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import objects.Board;
import objects.Game;
import objects.Move;
import objects.Tile;
import objects.Word;
import org.json.JSONObject;
import wordtrie.WordTrie;
import workers.Worker;

/**
 *
 * @author arouz
 */
public class GameManager {

    private SessionManager sm;

    public GameManager(SessionManager sm) {
        this.sm = sm;
    }

    public void playGames(ArrayList<Game> gameList) {
        // Iterate games, for each game:
        // * Proceed if it is your turn
        // * Based on game language, select trie and charscore set
        // * Use workers to find all moves
        // * Ask user which move to play
        // * Send solution to server

        // Init the threadpool
        ExecutorService pool = Executors.newFixedThreadPool(8);

        HashMap<Character, Integer> charScores = generateCharMap("en");
        WordTrie trie = generateWordTrie("en");

        for (Game game : gameList) {
            if (game.getCurrent_player() == game.getMy_position() && game.getEnd_game() == 0) {
                System.out.println("==========");
                System.out.println("GAME VS " + game.getEnemy_username());
                System.out.println(game.getMy_username() + "s score: " + game.getMy_score());
                System.out.println(game.getEnemy_username() + "s score: " + game.getEnemy_score());
                System.out.println("Calculating moves...");
                List<Move> validMoves = generateMoves(game, trie, charScores, pool);
                System.out.println("Type the index of the move you want me to play for you!");
                int i = 0;
                validMoves = validMoves.subList(0, 10);
                for (Move move : validMoves) {
                    System.out.println("[" + i + "] " + move.getWord().getWord() + " for " + move.getScore() + " points.");
                    i++;
                }
                Scanner scan = new Scanner(System.in);
                int choice = 0;
                while (true) {
                    try {
                        choice = scan.nextInt();
                        break;
                    } catch (InputMismatchException ex) {
                    }
                }
                Move chosenMove = validMoves.get(choice);
                JSONObject response = null;
                try {
                    response = sm.playMove(game.getId(), game.getRuleset(), chosenMove.getTiles(), chosenMove.getWords());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (response != null) {
                    if (response.get("status").equals("success")) {
                        System.out.println("Success!");
                        JSONObject content = (JSONObject) response.get("content");
                        System.out.println("I played " + content.get("main_word") + " for " + content.get("points") + " points for you. :)");
                    }
                }
            }
        }
        pool.shutdown();
    }

    private ArrayList<Move> generateMoves(Game game, WordTrie trie, HashMap<Character, Integer> charScores, ExecutorService pool) {
        boolean rotated = false;

        // The Set each runnable will add moves to
        Set<Move> validMoves = Collections.synchronizedSet(new HashSet<Move>());

        long startTime = System.currentTimeMillis();
        fetchHorizontalMoves(game, trie, charScores, rotated, pool, validMoves);
        game.getBoard().rotateBoard(false);
        rotated = true;
        fetchHorizontalMoves(game, trie, charScores, rotated, pool, validMoves);
        long endTime = System.currentTimeMillis();
        System.out.println("Moves generated in: " + (endTime - startTime) + "ms.");
        //validMoves.addAll(moves);

        ArrayList<Move> sortedMoves = new ArrayList<>(validMoves);
        Collections.sort(sortedMoves);
        return sortedMoves;
    }

    private void fetchHorizontalMoves(Game game, WordTrie trie, HashMap<Character, Integer> charScores, boolean rotated, ExecutorService pool, Set validMoves) {
        /*
        Iterate board
        For every row on the board, create a runnable
        Put runnable in queue
        Fetch valid words that can be formed on the board
        Validate the moves and calculate the score
        Add moves to validMoves set
         */

        ArrayList<String> rack = (ArrayList<String>) game.getRack();

        // Start with finding all words that can be created with only the rack, without interference of the board
        ArrayList<String> rackWords = trie.generateWordsWithRack(rack);

        // Check if this is the first move of the game. If center tile is empty, we set this as anchorTile
        if (game.getBoard().getTile(7, 7).getLetter().equals("_")) {
            for (String word : rackWords) {
                // If all tiles are used, 40 bonus points are awarded
                int bingo = 0;
                if (word.length() == 7) {
                    bingo += 40;
                }
                Move move = new Move(new Word(word));
                move.setScore(addScore(7, 7, false, word, charScores, game.getBoard()) + bingo);
                move.setEnds(new Tile(7, 6 + word.length()));
                move.setStarts(new Tile(7, 7));
                validMoves.add(move);
            }
        } else {
            Collection<Callable<Object>> callables = new ArrayList<>();

            // Iterate all positions in board
            for (int row = 0; row < 15; row++) {
                Runnable runnable = new Worker(game, row, rackWords, rotated, validMoves, trie, charScores, rack);
                Callable<Object> c = Executors.callable(runnable);
                callables.add(c);
            }

            try {
                pool.invokeAll(callables);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Generate HashMap with a character as key, and its score as value. Values
     * and keys given from external txt file.
     *
     * @return HashMap< Character,Integer >
     */
    private HashMap<Character, Integer> generateCharMap(String language) {
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

    public WordTrie generateWordTrie(String language) {
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
        return trie;
    }

    public int addScore(int row, int column, boolean horizontal, String word, HashMap<Character, Integer> charScores, Board board) {
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
}
