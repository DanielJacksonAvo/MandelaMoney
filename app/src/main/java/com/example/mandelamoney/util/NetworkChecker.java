package com.example.mandelamoney.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkChecker {
    private static final String urlString = "https://www.jacksonserver.ddns.net";

    public static String checkConnection() {
        try {
            URL url = new URL(urlString);
            long startTime = System.currentTimeMillis();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(5000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (responseCode >= 400) {
                return "Bad"; // Server responded with error
            }

            if (duration < 100) {
                return "Good";
            } else if (duration < 300) {
                return "Okay";
            } else {
                return "Bad";
            }

        } catch (
                IOException e) {
            return "Disconnected"; // Could not reach the server
        }
    }

}
