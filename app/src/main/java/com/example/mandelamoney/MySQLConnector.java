package com.example.mandelamoney;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLConnector {
    private final static String DB_URL = BuildConfig.DB_URL;
    private final static String DB_USERNAME = BuildConfig.DB_USERNAME;
    private final static String DB_PASSWORD = BuildConfig.DB_PASSWORD;
    private static Connection connection;

    public static boolean connectToDB() {
        connection = null;

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

        return connection != null;

    }

    private static ResultSet executeQuery(String query, Context context) {
        while (connection == null || !isConnected()) {
            Log.e("MySQL", "Not connected to database. Attempting to reconnect...");
            // Display a Toast message to the user indicating connection attempt.
            Toast.makeText(context, "Failed to connect. Trying again...", Toast.LENGTH_LONG).show();
            // Attempt to establish a new connection.
            if (!connectToDB()) {
                // If reconnection fails, wait a bit before retrying to avoid busy-looping.
                try {
                    Thread.sleep(2000); // Wait for 2 seconds
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    Log.e("MySQL", "Interrupted during reconnection wait: " + ie.getMessage());
                    return null; // Exit if interrupted
                }
            }
        }

        try {
            // Create a statement and execute the query.
            return connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            // Log any SQL errors that occur during query execution.
            Log.e("MySQL", "Error executing select query: " + e.getMessage());
            return null;
        }
    }

    private static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            Log.e("MySQL", "Error checking connection status: " + e.getMessage());
            return false;
        }
    }

    public static ResultSet validateEmailPassword(String userEmail, String userPassword, Context context) {
        while (connection == null || !isConnected()) {
            Log.e("MySQL", "Not connected to database for procedure call. Attempting to reconnect...");
            Toast.makeText(context, "Failed to connect. Trying again...", Toast.LENGTH_LONG).show();
            if (!connectToDB()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    Log.e("MySQL", "Interrupted during reconnection wait for procedure call: " + ie.getMessage());
                    return null;
                }
            }
        }

        CallableStatement callableStatement = null;
        ResultSet resultSet = null;

        try {
            String callProcedureSQL = "{CALL ValidateEmailPassword(?, ?)}";
            callableStatement = connection.prepareCall(callProcedureSQL);

            callableStatement.setString(1, userEmail);
            callableStatement.setString(2, userPassword);

            boolean hasResultSet = callableStatement.execute();

            if (hasResultSet) {
                resultSet = callableStatement.getResultSet();
            } else {
                Log.d("MySQL", "Stored procedure 'ValidateEmailPassword' did not return a ResultSet.");
            }

        } catch (SQLException e) {
            Log.e("MySQL", "Error calling stored procedure 'ValidateEmailPassword': " + e.getMessage());
        } finally {
            if (callableStatement != null) {
                try {
                    callableStatement.close();
                } catch (SQLException e) {
                    Log.e("MySQL", "Error closing CallableStatement: " + e.getMessage());
                }
            }
        }
        return resultSet;
    }


}
