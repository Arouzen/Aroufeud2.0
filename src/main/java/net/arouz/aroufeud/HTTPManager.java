package net.arouz.aroufeud;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author arouz
 */
public class HTTPManager {

    private final CloseableHttpClient httpClient;
    private final HttpClientConnectionManager connManager;
    private String sessionId;

    public HTTPManager() {
        connManager = new BasicHttpClientConnectionManager();
        httpClient = HttpClientBuilder.create().setConnectionManager(connManager).setRedirectStrategy(new LaxRedirectStrategy()).build();
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public JSONObject postJson(final String path, final String data) throws Exception {
        try {
            // Convert string data to HttpEntity object
            StringEntity entity = new StringEntity(data, "UTF-8");
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            // Create HttpPost object
            final HttpPost post = createPost(path, entity);

            // Add sessionid cookie to HttpPost object
            if (sessionId != null) {
                post.addHeader("Cookie", "sessionid=" + sessionId);
            }

            // Send post to server, fetch HttpResponse
            final HttpResponse response = httpClient.execute(post);

            // Handle HttpResponse
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return handleResponse(response);
            } else {
                EntityUtils.consume(response.getEntity());
                throw new Exception("Got unexpected HTTP " + response.getStatusLine().getStatusCode() + ": " + response.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException("Error when contacting WordFeud API", e);
        } catch (JSONException e) {
            throw new RuntimeException("Could not parse JSON", e);
        } finally {
            connManager.closeExpiredConnections();
        }
    }

    private JSONObject handleResponse(final HttpResponse response) throws IOException, JSONException {
        final Header[] cookies = response.getHeaders("Set-Cookie");
        if (cookies != null && cookies.length > 0) {
            sessionId = extractSessionIdFromCookie(cookies[0]);
        }

        final String responseString = EntityUtils.toString(response.getEntity());
        return new JSONObject(responseString);
    }

    private String extractSessionIdFromCookie(final Header cookie) {
        final String cookieValue = cookie.getValue();
        final Matcher matcher = Pattern.compile("sessionid=(.*?);").matcher(cookieValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private HttpPost createPost(final String path, final HttpEntity entity) throws UnsupportedEncodingException {
        //System.out.println("http://api.wordfeud.com/wf" + path);
        final HttpPost post = new HttpPost("http://api.wordfeud.com/wf" + path);
        post.addHeader("User-Agent", "WebFeudClient/2.8.0 (Android 5.1.1)"); // Last changed 10/3-2016
        post.addHeader("Connection", "Keep-Alive");
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept-Encoding", "gzip");
        post.setEntity(entity);
        post.setProtocolVersion(new ProtocolVersion("HTTP", 1, 1));
        return post;
    }
}