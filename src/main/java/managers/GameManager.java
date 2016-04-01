package managers;

import aroufeud.Aroufeud;
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

    private final SessionManager sm;
    HashMap<Character, Integer> en_charScores;
    HashMap<Character, Integer> sv_charScores;
    WordTrie en_trie;
    WordTrie sv_trie;
    ExecutorService pool;

    public GameManager(Aroufeud aroufeud) {
        this.sm = aroufeud.getSessionManager();
        this.en_charScores = aroufeud.get_en_charScores();
        this.en_trie = aroufeud.get_en_trie();
        this.sv_charScores = aroufeud.get_sv_charScores();
        this.sv_trie = aroufeud.get_sv_trie();
        this.pool = aroufeud.getPool();
    }

    public void playGames(ArrayList<Game> gameList, boolean fullyAutomatic) {
        // Iterate games, for each game:
        // * Proceed if it is your turn
        // * Based on game language, select trie and charscore set
        // * Use workers to find all moves
        // * [if not fully automatic] Ask user which move to play
        // * [if fully automatic] Iterate the moves, try to play the best, else keep moving
        // * Send solution to server
        boolean playedOnce = false;
        for (Game game : gameList) {
            if (game.getCurrent_player() == game.getMy_position() && game.getEnd_game() == 0) {
                playedOnce = true;
                System.out.println("==========");
                String enemy = (game.getEnemy_fullname().isEmpty() ? game.getEnemy_username() : game.getEnemy_fullname());
                System.out.println(game.getMy_username() + " VS " + enemy);
                System.out.println(game.getMy_username() + "s score: " + game.getMy_score());
                System.out.println(enemy + "s score: " + game.getEnemy_score());
                System.out.println("Calculating moves...");
                List<Move> validMoves = generateMoves(game, (game.getRuleset() == 0 ? en_trie : sv_trie), (game.getRuleset() == 0 ? en_charScores : sv_charScores), pool);
                Move chosenMove;
                if (!fullyAutomatic) {
                    System.out.println("Type the index of the move you want me to play for you!");
                    int i = 0;
                    // If over 10 valid moves, strip it down to 10
                    if (validMoves.size() >= 10) {
                        validMoves = validMoves.subList(0, 10);
                    }
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
                    chosenMove = validMoves.get(choice);
                    JSONObject response = null;
                    try {
                        response = sm.playMove(game.getId(), game.getRuleset(), chosenMove.getTiles(), chosenMove.getWords());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (response != null) {
                        if (response.get("status").equals("success")) {
                            //System.out.println("Success!");
                            JSONObject content = (JSONObject) response.get("content");
                            System.out.println("I played " + content.get("main_word") + " for " + content.get("points") + " points for you. :)");
                        } else {
                            System.out.println(response.toString());
                            System.out.println(chosenMove);
                        }
                    }
                } else {
                    // Automatic moves
                    for (Move move : validMoves) {
                        JSONObject response = null;
                        try {
                            response = sm.playMove(game.getId(), game.getRuleset(), move.getTiles(), move.getWords());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        if (response != null) {
                            if (response.get("status").equals("success")) {
                                //System.out.println("Success!");
                                JSONObject content = (JSONObject) response.get("content");
                                System.out.println("I played " + content.get("main_word") + " for " + content.get("points") + " points for you. :)");
                                break;
                            } else {
                                System.out.println(response.toString());
                                System.out.println("Trying next move...");
                            }
                        }
                    }
                }
            }
        }
        if (!playedOnce) {
            System.out.println("Waiting for opponents...");
        }
    }

    private ArrayList<Move> generateMoves(Game game, WordTrie trie, HashMap<Character, Integer> charScores, ExecutorService pool) {
        boolean rotated = false;

        // The Set each runnable will add moves to
        Set<Move> validMoves = Collections.synchronizedSet(new HashSet<Move>());

        long startTime = System.currentTimeMillis();
        fetchHorizontalMoves(game, trie, charScores, rotated, pool, validMoves);
        game.getBoard().rotateCCWAndFlipBoard();
        rotated = true;
        fetchHorizontalMoves(game, trie, charScores, rotated, pool, validMoves);
        long endTime = System.currentTimeMillis();
        System.out.println("Solutions generated in: " + (endTime - startTime) + "ms.");

        ArrayList<Move> sortedMoves = new ArrayList<>();
        // Iterate the moves
        for (Move move : validMoves) {
            // Add unique moves to sortedMoves
            if (!sortedMoves.contains(move)) {
                sortedMoves.add(move);
            }
        }
        // Then sort the list and return it
        Collections.sort(sortedMoves);
        return sortedMoves;
    }

    private void fetchHorizontalMoves(Game game, WordTrie trie, HashMap<Character, Integer> charScores, boolean rotated, ExecutorService pool, Set<Move> validMoves) {
        /*
        Iterate board
        For every row on the board, create a runnable
        Put runnable in queue
        Fetch valid words that can be formed on the board
        Validate the moves and calculate the score
        Add moves to validMoves set
         */

        ArrayList<String> rack = game.getRack();

        // Start with finding all words that can be created with only the rack, without interference of the board
        ArrayList<String> rackWords = trie.generateWordsWithRack(rack);

        // Check if this is the first move of the game. If center tile is empty, we set this as anchorTile
        if (game.getBoard().getTile(7, 7).getLetter().equals("_")) {
            for (String word : rackWords) {
                // If all tiles are used, 40 bonus points are awarded. Because this is the first move, we know for sure we have used all tiles if the word is 7 long.
                int bingo = 0;
                if (word.length() == 7) {
                    bingo += 40;
                }
                Move move = new Move(new Word(word));
                move.setScore(addScore(7, 7, false, word, charScores, game.getBoard()) + bingo);
                move.addWord(word);
                int column = 7;
                for (char character : word.toCharArray()) {
                    move.addTile(7, column, String.valueOf(character),
                            !rack.contains(String.valueOf(character))); // If the rack doesn't contain the character, it's a wildcard character thus true
                    column++;
                }
                validMoves.add(move);
            }
        } else {
            Collection<Callable<Object>> callables = new ArrayList<>();

            // Iterate all positions in board
            for (int row = 0; row < 15; row++) {
                Runnable runnable = new Worker(game, row, rackWords, rotated, validMoves, trie, charScores, rack);
                callables.add(Executors.callable(runnable));
            }

            try {
                pool.invokeAll(callables);
            } catch (Exception ex) {
                System.out.println("Something went wrong when invoking the callables.");
            }
        }
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
            totScore += charScore;
        }
        return totScore * multiplier;
    }
}
