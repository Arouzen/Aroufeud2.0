package managers;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author arouz
 */
public class SessionManager {

    private final HTTPManager httpManager;

    public SessionManager() {
        httpManager = new HTTPManager();
    }

    public JSONObject doLogin(String email, String pass) throws Exception {
        String jsonData = "{"
                + "\"password\": \"" + encryptPassword(pass) + "\","
                + "\"email\": \"" + email + "\""
                + "}";
        return httpManager.postJson("/user/login/email/", jsonData);
    }

    public JSONObject getStatus() throws Exception {
        return httpManager.postJson("/user/status/", "");
    }

    public JSONObject getGameDetails(String ids) throws Exception {
        return httpManager.postJson("/games/" + ids, "");
    }

    public JSONObject playMove(long gameId, int ruleset, JSONArray move, JSONArray word) throws Exception {
        String jsonData = "{"
                + "\"ruleset\": " + ruleset + ","
                + "\"words\": " + word.toString() + ","
                + "\"move\": " + move.toString() + ""
                + "}";

        //System.out.println(jsonData);
        //return null;
        return httpManager.postJson("/game/" + gameId + "/move/", jsonData);
    }

    private static String encryptPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update((password + "JarJarBinks9").getBytes("UTF-8"));

        return new BigInteger(1, crypt.digest()).toString(16);
    }

    public JSONObject rejectInvite(long id) throws Exception {
        return httpManager.postJson("/games/" + String.valueOf(id) + "/reject/", "");
    }

    public JSONObject acceptInvite(long id) throws Exception {
        return httpManager.postJson("/invite/" + String.valueOf(id) + "/accept/", "");
    }
}
