/*
 */
package objects;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author arouz
 */
public final class Game {

    private final Board board;

    private int end_game;
    private long created;

    private int my_position;
    private int my_score;
    private String my_username;
    private long my_id;

    private int enemy_score;
    private int enemy_position;
    private String enemy_username;
    private String enemy_fullname;
    private long enemy_id;

    private ArrayList<String> rack;

    private int ruleset;
    private int current_player;
    private long id;

    public Game(JSONObject game, String my_username) {
        this.end_game = game.getInt("end_game");
        this.created = game.getLong("created");
        this.my_username = my_username;

        JSONArray playerArray = game.getJSONArray("players");
        for (Object tmpPlayer : playerArray) {
            JSONObject playerJson = (JSONObject) tmpPlayer;

            // Me
            if (my_username.equals(playerJson.getString("username"))) {
                setRack(playerJson.getJSONArray("rack"));
                this.my_position = playerJson.getInt("position");
                this.my_score = playerJson.getInt("score");
                this.my_id = playerJson.getLong("id");
            } else {
                // Enemy
                this.enemy_score = playerJson.getInt("score");
                this.enemy_position = playerJson.getInt("position");
                this.enemy_username = playerJson.getString("username");
                this.enemy_fullname = (!playerJson.isNull("fb_first_name") ? playerJson.getString("fb_first_name") + " " + playerJson.getString("fb_last_name") : "");
                this.enemy_id = playerJson.getLong("id");
            }
        }

        this.ruleset = game.getInt("ruleset");
        this.current_player = game.getInt("current_player");
        this.id = game.getLong("id");
        this.board = new Board(game.getJSONArray("tiles"));
    }

    public void setRack(JSONArray rack) {

        ArrayList<String> tmpRack = new ArrayList<>();
        int i = 0;
        for (Object letter : rack) {
            tmpRack.add(String.valueOf(letter).toLowerCase());
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

    public ArrayList<String> getRack() {
        return new ArrayList<>(rack);
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

    /**
     * @param end_game the end_game to set
     */
    public void setEnd_game(int end_game) {
        this.end_game = end_game;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(long created) {
        this.created = created;
    }

    /**
     * @param my_position the my_position to set
     */
    public void setMy_position(int my_position) {
        this.my_position = my_position;
    }

    /**
     * @param my_score the my_score to set
     */
    public void setMy_score(int my_score) {
        this.my_score = my_score;
    }

    /**
     * @param my_username the my_username to set
     */
    public void setMy_username(String my_username) {
        this.my_username = my_username;
    }

    /**
     * @return the my_id
     */
    public long getMy_id() {
        return my_id;
    }

    /**
     * @param my_id the my_id to set
     */
    public void setMy_id(long my_id) {
        this.my_id = my_id;
    }

    /**
     * @param enemy_score the enemy_score to set
     */
    public void setEnemy_score(int enemy_score) {
        this.enemy_score = enemy_score;
    }

    /**
     * @param enemy_position the enemy_position to set
     */
    public void setEnemy_position(int enemy_position) {
        this.enemy_position = enemy_position;
    }

    /**
     * @param enemy_username the enemy_username to set
     */
    public void setEnemy_username(String enemy_username) {
        this.enemy_username = enemy_username;
    }

    /**
     * @return the enemy_fullname
     */
    public String getEnemy_fullname() {
        return enemy_fullname;
    }

    /**
     * @param enemy_fullname the enemy_fullname to set
     */
    public void setEnemy_fullname(String enemy_fullname) {
        this.enemy_fullname = enemy_fullname;
    }

    /**
     * @return the enemy_id
     */
    public long getEnemy_id() {
        return enemy_id;
    }

    /**
     * @param enemy_id the enemy_id to set
     */
    public void setEnemy_id(long enemy_id) {
        this.enemy_id = enemy_id;
    }

    /**
     * @param ruleset the ruleset to set
     */
    public void setRuleset(int ruleset) {
        this.ruleset = ruleset;
    }

    /**
     * @param current_player the current_player to set
     */
    public void setCurrent_player(int current_player) {
        this.current_player = current_player;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }
}
