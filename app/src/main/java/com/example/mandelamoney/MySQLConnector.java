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

    private static volatile Connection connection;

    private final static int CONNECTION_TIMEOUT_SECONDS = 7;
    private final static int MAX_RETRIES = 3;
    private final static long RETRY_DELAY_MS = 2000;

    private MySQLConnector() {
    }

    private static synchronized boolean connectToDB(Context context) {
        if (connection != null) {
            try {
                if (connection.isValid(CONNECTION_TIMEOUT_SECONDS)) {
                    Log.d("MySQLConnector", "Existing connection is valid.");
                    return true;
                } else {
                    Log.d("MySQLConnector", "Existing connection is invalid. Closing and reconnecting.");
                    closeConnection();
                }
            } catch (SQLException e) {
                Log.e("MySQLConnector", "Error checking existing connection validity: " + e.getMessage());
                closeConnection();
            }
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                Log.d("MySQLConnector", "Attempting to connect to DB. Attempt " + (attempts + 1) + " of " + MAX_RETRIES);
                if (attempts != 0) {
                    Toast.makeText(context,"Database connection lost. Reconnecting...", Toast.LENGTH_SHORT).show();
                }

                Class.forName("com.mysql.jdbc.Driver");

                DriverManager.setLoginTimeout(CONNECTION_TIMEOUT_SECONDS);

                connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

                if (connection != null && connection.isValid(CONNECTION_TIMEOUT_SECONDS)) {
                    Log.d("MySQLConnector", "Connected successfully to database.");
                    if(attempts != 0) {
                        Toast.makeText(context,"Connected successfully to database.", Toast.LENGTH_LONG).show();
                    }
                    return true;
                } else {
                    Log.e("MySQLConnector", "Connection failed: Connection object is null or invalid.");
                }

            } catch (SQLException e) {
                Log.e("MySQLConnector", "SQLException during connection attempt: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e("MySQLConnector", "JDBC Driver not found: " + e.getMessage());
                break;
            } catch (Exception e) {
                Log.e("MySQLConnector", "Unexpected Exception during connection attempt: " + e.getMessage());
            }

            attempts++;
            if (attempts < MAX_RETRIES) {
                try {
                    Log.d("MySQLConnector", "Connection failed. Retrying in " + RETRY_DELAY_MS + "ms...");
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    Log.e("MySQLConnector", "Connection retry interrupted: " + ie.getMessage());
                    break;
                }
            }
        }
        Log.e("MySQLConnector", "Failed to connect to database after " + MAX_RETRIES + " attempts.");
        Toast.makeText(context, "Failed to connect to database.",Toast.LENGTH_LONG).show();
        return false;
    }

    public static void AppStartUpConnection(Context context) {
        connectToDB(context);
    }

    private static synchronized Connection getConnection(Context context) {
        try {
            if (connection == null || !connection.isValid(CONNECTION_TIMEOUT_SECONDS)) {
                Log.d("MySQLConnector", "Connection is null or invalid. Attempting to reconnect.");
                if (context != null) {
                    Toast.makeText(context.getApplicationContext(), "Database connection lost. Reconnecting...", Toast.LENGTH_LONG).show();
                }
                if (!connectToDB(context)) {
                    Log.e("MySQLConnector", "Failed to establish a new connection.");
                    if (context != null) {
                        Toast.makeText(context.getApplicationContext(), "Failed to connect to database.", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error checking connection validity in getConnection: " + e.getMessage());
            if (!connectToDB(context)) {
                Log.e("MySQLConnector", "Failed to establish a new connection after validity check error.");
                if (context != null) {
                    Toast.makeText(context.getApplicationContext(), "Failed to connect to database.", Toast.LENGTH_LONG).show();
                }
                return null;
            }
        }
        return connection;
    }

    public static ResultSet executeQuery(String query, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot execute query: No valid database connection.");
            return null;
        }

        try {
            return currentConnection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error executing select query: " + e.getMessage());
            return null;
        }
    }

    public static Object[] validateEmailPassword(String userEmail, String userPassword, Context context) {
        Connection currentConnection = getConnection(context);
        User user = null;
        Object[] objs = new Object[2];
        boolean connectionEstablished = false;
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot validate email/password: No valid database connection.");
            objs[0] = user;
            objs[1] = connectionEstablished;
            return null;
        }
        connectionEstablished = true;


        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL MandelaMoneyDB.ValidateEmailPassword(?, ?)}")) {

            callableStatement.setString(1, userEmail);
            callableStatement.setString(2, userPassword);

            Log.d("MySQLConnector", "Calling ValidateEmailPassword for user: " + userEmail);
            boolean hasResultSet = callableStatement.execute();

            if (hasResultSet) {
                try (ResultSet resultSet = callableStatement.getResultSet()) {
                    user = ResultSetParser.parseValidateEmailPassword(resultSet, userEmail, userPassword);
                    objs[0] = user;
                    objs[1] = connectionEstablished;
                }
            } else {
                Log.d("MySQLConnector", "Stored procedure 'ValidateEmailPassword' did not return a ResultSet.");
            }

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'ValidateEmailPassword': " + e.getMessage());
        }
        return objs;
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                Log.d("MySQLConnector", "Database connection closed.");
            } catch (SQLException e) {
                Log.e("MySQLConnector", "Error closing database connection: " + e.getMessage());
            }
        }
    }
}