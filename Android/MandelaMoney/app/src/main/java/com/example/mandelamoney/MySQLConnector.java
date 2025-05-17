package com.example.mandelamoney;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnector {
    private static final String DB_URL = "jdbc:mysql://jacksonserver.ddns.net:3306/MandelaMoneyDB"
            + "?useSSL=true"
            + "&requireSSL=true"
            + "&verifyServerCertificate=false";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "J!Hs7#BJv&tCmyhA6h^xd3AXtpnEWUe5";

    public static Connection connectToDB() {
        Connection connection = null;

        try {
            // Allow network operations on the main thread (only for testing)
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Load the MySQL JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // Connect to the database
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            if (connection != null) {
                Log.d("MySQL", "Connected successfully.");
            } else {
                Log.e("MySQL", "Connection failed.");
            }

        } catch (SQLException e) {
            Log.e("MySQL", "SQLException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("MySQL", "JDBC Driver not found: " + e.getMessage());
        } catch (Exception e) {
            Log.e("MySQL", "Exception: " + e.getMessage());
        }

        return connection;
    }
}
