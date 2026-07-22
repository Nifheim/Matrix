package net.nifheim.matrix.common.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

/**
 * HTTP utilities.
 *
 * @author Jaime Suárez
 */
public final class HTTPUtils {

    private HTTPUtils() {
    }

    /**
     * Sends a GET request to the specified URL.
     *
     * @param urlString the URL to send the request to
     * @throws Exception if the request fails
     */
    public static void GET(@NotNull String urlString) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        try {
            request.setRequestMethod("GET");
            request.connect();
            if (request.getResponseCode() < 200 || request.getResponseCode() > 299) {
                throw new RuntimeException("Failed : HTTP error code : " + request.getResponseCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            request.disconnect();
        }
    }
}
