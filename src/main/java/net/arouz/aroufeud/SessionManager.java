package net.arouz.aroufeud;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        
        System.out.println(jsonData);
        return httpManager.postJson("/user/login/email/", jsonData);
    }
    
    public JSONObject getGames() throws Exception {
        return httpManager.postJson("/user/games/", "");
    }

    private static String encryptPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update((password + "JarJarBinks9").getBytes("UTF-8"));

        return new BigInteger(1, crypt.digest()).toString(16);
    }
}
