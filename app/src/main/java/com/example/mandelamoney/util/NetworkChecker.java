package com.example.mandelamoney.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetworkChecker {
    private static final String host = "www.jacksonserver.ddns.net";
    private static final int port = 3306; // MySQL default port

    public static String checkConnection() {
        try {
            long startTime = System.currentTimeMillis();

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000); // 5 seconds timeout
            socket.close();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            if (duration < 100) {
                return "Good";
            } else if (duration < 300) {
                return "Okay";
            } else {
                return "Bad";
            }

        } catch (IOException e) {
            return "Disconnected"; // Could not reach the server or timed out
        }
    }
}
