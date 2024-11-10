package net.nifheim.matrix.common.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        URL url = new URL(urlString);
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

    /**
     * Sends a POST request to the specified URL.
     *
     * @param urlString the URL to send the request to
     * @param data      the data to send
     * @throws Exception if the request fails
     */
    public static void POST(@NotNull String urlString, @NotNull String data) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        try {
            request.setRequestMethod("POST");
            request.setDoOutput(true);
            request.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
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
