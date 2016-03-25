/*
 */
package objects;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author arouz
 */
public final class Game {

    Board board;
    
    int end_game;
    long created;

    int my_position;
    int my_score;
    String my_username;

    int enemy_score;
    int enemy_position;
    String enemy_username;

    String[] rack;

    int ruleset;
    int current_player;
    long id;

    public Game(JSONObject game, String my_username) {
        this.end_game = game.getInt("end_game");
        this.created = game.getLong("created");
        this.my_username = my_username;

        JSONArray playerArray = game.getJSONArray("players");
        for (Object tmpPlayer : playerArray) {
            JSONObject playerJson = (JSONObject) tmpPlayer;

            // Is player me?
            if (my_username.equals(playerJson.getString("username"))) {
                setRack(playerJson.getJSONArray("rack"));
                this.my_position = playerJson.getInt("position");
                this.my_score = playerJson.getInt("score");
            } else {
                // enemy
                this.enemy_score = playerJson.getInt("score");
                this.enemy_position = playerJson.getInt("position");
                this.enemy_username = playerJson.getString("username");
            }
        }

        this.ruleset = game.getInt("ruleset");
        this.current_player = game.getInt("current_player");
        this.id = game.getLong("id");
        this.board = new Board(game.getJSONArray("tiles"));
    }

    public void setRack(JSONArray rack) {
        String[] tmpRack = new String[rack.length()];
        int i = 0;
        for (Object letter : rack) {
            tmpRack[i] = String.valueOf(letter);
            i++;
        }
        this.rack = tmpRack;
    }

    public Board getBoard() {
        return board;
    }

    public int getEnd_game() {
        return end_game;
    }

    public long getCreated() {
        return created;
    }

    public int getMy_position() {
        return my_position;
    }

    public int getMy_score() {
        return my_score;
    }

    public String getMy_username() {
        return my_username;
    }

    public int getEnemy_score() {
        return enemy_score;
    }

    public int getEnemy_position() {
        return enemy_position;
    }

    public String getEnemy_username() {
        return enemy_username;
    }

    public String[] getRack() {
        return rack;
    }

    public int getRuleset() {
        return ruleset;
    }

    public int getCurrent_player() {
        return current_player;
    }

    public long getId() {
        return id;
    }
}