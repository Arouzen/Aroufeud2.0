package aroufeud;

import java.util.ArrayList;
import managers.SessionManager;
import objects.Game;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author arouz
 */
public class Aroufeud {

    SessionManager sm;

    public Aroufeud() {
        sm = new SessionManager();
    }

    public JSONObject login(String email, String password) throws Exception {
        return sm.doLogin(email, password);
    }

    public ArrayList<Game> parseStatus(String my_username) throws Exception {
        JSONObject response = sm.getStatus();
        if (response.get("status").equals("success")) {
            response = (JSONObject) response.get("content");
            parseInvites((JSONArray) response.get("invites_received"));
            return parseGames((JSONArray) response.get("games"), my_username);
        } else {
            System.out.println(response.toString());
        }
        return null;
    }

    private void parseInvites(JSONArray inviteArray) {
        for (Object obj : inviteArray) {
            JSONObject invite = (JSONObject) obj;
            if (invite.getString("board_type").equals("normal")) {
                if (invite.getInt("ruleset") == 4 || invite.getInt("ruleset") == 0) {
                    try {
                        sm.acceptInvite(invite.getLong("id"));
                        System.out.println("Accepted invite from " + invite.getString("inviter") + "!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                } else {
                    try {
                        sm.rejectInvite(invite.getLong("id"));
                        System.out.println("Rejected invite from " + invite.getString("inviter") + "! Reason: ruleset");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    sm.rejectInvite(invite.getLong("id"));
                    System.out.println("Rejected invite from " + invite.getString("inviter") + "! Reason: board_type");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private ArrayList<Game> parseGames(JSONArray gameArray, String my_username) {
        String ids = "";
        for (Object game : gameArray) {
            String id = String.valueOf(((JSONObject) game).get("id"));
            ids += id + ",";
        }
        // Remove last comma
        ids = ids.substring(0, ids.length() - 1);

        JSONObject gameDetails = null;
        try {
            gameDetails = sm.getGameDetails(ids);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ArrayList<Game> gameList = new ArrayList<>();

        if (gameDetails != null && gameDetails.get("status").equals("success")) {
            gameDetails = (JSONObject) gameDetails.get("content");
            JSONArray games = gameDetails.getJSONArray("games");
            for (Object game : games) {
                gameList.add(new Game((JSONObject) game, my_username));
            }
        }

        return gameList;
    }
}
