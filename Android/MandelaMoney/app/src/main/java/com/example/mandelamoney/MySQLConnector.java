package com.example.mandelamoney;

import android.os.StrictMode;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnector {
    private final static String DB_URL = BuildConfig.DB_URL;
    private final static String DB_USERNAME = BuildConfig.DB_USERNAME;
    private final static String DB_PASSWORD = BuildConfig.DB_PASSWORD;

    public static Connection connectToDB() {
        Connection connection = null;

        try {
            // Allow network operations on the main thread (only for testing)
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // Load the MySQL JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // Connect to the database
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

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
