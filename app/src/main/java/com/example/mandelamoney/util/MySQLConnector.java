package com.example.mandelamoney.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.example.mandelamoney.BuildConfig;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MySQLConnector {
    private final static String DB_URL = BuildConfig.DB_URL;
    private final static String DB_USERNAME = BuildConfig.DB_USERNAME;
    private final static String DB_PASSWORD = BuildConfig.DB_PASSWORD;

    private static volatile Connection connection;

    private final static int CONNECTION_TIMEOUT_SECONDS = 7;
    private final static int MAX_RETRIES = 3;
    private final static long RETRY_DELAY_MS = 3000;

    private static Handler uiHandler;

    private MySQLConnector() {}

    public static synchronized void initializeUiHandler(Context context) {
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper());
        }
    }

    private static void showToastOnUI(Context context, String message, int duration) {
        if (uiHandler == null) {
            Log.e("MySQLConnector", "UI Handler not initialized. Cannot show Toast: " + message);
            return;
        }
        uiHandler.post(() -> Toast.makeText(context.getApplicationContext(), message, duration).show());
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
                    showToastOnUI(context, "Database connection lost. Reconnecting...", Toast.LENGTH_SHORT);
                }

                Class.forName("com.mysql.jdbc.Driver");
                DriverManager.setLoginTimeout(CONNECTION_TIMEOUT_SECONDS);
                connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

                if (connection != null && connection.isValid(CONNECTION_TIMEOUT_SECONDS)) {
                    Log.d("MySQLConnector", "Connected successfully to database.");
                    if(attempts != 0) {
                        showToastOnUI(context, "Connected successfully to database.", Toast.LENGTH_LONG);
                    }
                    return true;
                } else {
                    Log.e("MySQLConnector", "Connection failed: Connection object is null or invalid.");
                }

            } catch (SQLException e) {
                Log.e("MySQLConnector", "SQLException during connection attempt: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.e("MySQLConnector", "JDBC Driver not found: " + e.getMessage());
                showToastOnUI(context, "App Error: Database driver missing!", Toast.LENGTH_LONG);
                break;
            } catch (Exception e) {
                Log.e("MySQLConnector", "Unexpected Exception during connection attempt: " + e.getMessage());
                showToastOnUI(context, "Connection error: " + e.getMessage(), Toast.LENGTH_LONG);
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
        showToastOnUI(context, "Failed to connect to database.",Toast.LENGTH_LONG);
        return false;
    }

    public static Boolean AppStartUpConnection(Context context) {
        initializeUiHandler(context.getApplicationContext());
        return connectToDB(context);
    }

    private static synchronized Connection getConnection(Context context) {
        try {
            if (connection == null || !connection.isValid(CONNECTION_TIMEOUT_SECONDS)) {
                Log.d("MySQLConnector", "Connection is null or invalid. Attempting to reconnect.");
                showToastOnUI(context, "Database connection lost. Reconnecting...", Toast.LENGTH_LONG);

                if (!connectToDB(context)) {
                    Log.e("MySQLConnector", "Failed to establish a new connection.");
                    showToastOnUI(context, "Failed to connect to database.", Toast.LENGTH_LONG);
                    return null;
                }
            }
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error checking connection validity in getConnection: " + e.getMessage());
            if (!connectToDB(context)) {
                Log.e("MySQLConnector", "Failed to establish a new connection after validity check error.");
                showToastOnUI(context, "Failed to connect to database.", Toast.LENGTH_LONG);
                return null;
            }
        }
        return connection;
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
            return objs;
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
            objs[0] = null;
            objs[1] = connectionEstablished;
        }
        return objs;
    }
    public static Object[] getRecoveryCodeHash(String userEmail, Context context) {
        Connection currentConnection = getConnection(context);
        Object[] result = new Object[3];
        boolean connectionEstablished;

        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot return recovery code: No valid database connection.");
            result[0] = false;
            result[1] = null;
            result[2] = false;
            return result;
        }
        connectionEstablished = true;

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL MandelaMoneyDB.GetRecoveryCodeHash(?, ?, ?)}")) {

            callableStatement.setString(1, userEmail);
            callableStatement.registerOutParameter(2, java.sql.Types.BOOLEAN);
            callableStatement.registerOutParameter(3, java.sql.Types.VARCHAR);

            Log.d("MySQLConnector", "Calling GetRecoveryCodeHash for user: " + userEmail);
            callableStatement.execute();

            boolean userExists = callableStatement.getBoolean(2);
            String hashCode = callableStatement.getString(3);

            result[0] = userExists;
            result[1] = hashCode;
            result[2] = connectionEstablished;

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'GetRecoveryCodeHash': " + e.getMessage());
            result[0] = false;
            result[1] = null;
            result[2] = connectionEstablished;
        }

        return result;
    }
    public static boolean verifyRecoveryCode(String userEmail, String recoveryCode, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "No valid DB connection.");
            return false;
        }

        boolean isMatch = false;

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL MandelaMoneyDB.VerifyRecoveryCode(?, ?, ?)}")) {

            callableStatement.setString(1, userEmail);
            callableStatement.setString(2, recoveryCode);
            callableStatement.registerOutParameter(3, java.sql.Types.BOOLEAN);

            Log.d("MySQLConnector", "Calling VerifyRecoveryCode for user: " + userEmail);
            callableStatement.execute();

            isMatch = callableStatement.getBoolean(3);
            Log.d("MySQLConnector", "Recovery code match result: " + isMatch);

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'VerifyRecoveryCode': " + e.getMessage());
        }

        return isMatch;
    }
    public static Object[] createDepositBankAndPendingTransaction(
            String userEmail,
            float amount,
            String baNumber,
            String baBranchCode,
            String baName,
            String baBank,
            Context context
    ) {
        Connection conn = getConnection(context);
        Object[] out = new Object[]{false, null, null};
        if (conn == null) return out;

        final String SQL = "{CALL MandelaMoneyDB.createDepositBankAndPendingTransaction(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (CallableStatement stmt = conn.prepareCall(SQL)) {
            int i = 1;
            stmt.setString(i++, userEmail);
          
            java.math.BigDecimal decAmount = java.math.BigDecimal.valueOf(
                    Math.round((double) amount * 100.0) / 100.0
            );
            stmt.setBigDecimal(i++, decAmount);

            stmt.setString(i++, baNumber);
            stmt.setString(i++, baBranchCode);
            stmt.setString(i++, baName);
            stmt.setString(i++, baBank);

            stmt.registerOutParameter(i++, java.sql.Types.BOOLEAN);
            stmt.registerOutParameter(i++, java.sql.Types.INTEGER);

            stmt.execute();

            boolean success = stmt.getBoolean(7);
            int txnId = stmt.getInt(8);
            Integer txnObj = stmt.wasNull() ? null : txnId;

            out[0] = success;
            out[1] = txnObj;
            out[2] = null;

        } catch (SQLException e) {
            Log.e("MySQLConnector", "createDepositBankAndPendingTransaction failed: " + e.getMessage(), e);
        }
        return out;
    }
    public static Object[] createWithdrawBankAndPendingTransaction(
            String userEmail,
            float amount,
            String baNumber,
            String baBranchCode,
            String baName,
            String baBank,
            Context context
    ) {
        Connection conn = getConnection(context);
        Object[] out = new Object[]{false, null, null};
        if (conn == null) return out;
        final String SQL = "{CALL MandelaMoneyDB.createWithdrawBankAndPendingTransaction(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (CallableStatement stmt = conn.prepareCall(SQL)) {
            int i = 1;
            stmt.setString(i++, userEmail);

            java.math.BigDecimal decAmount = java.math.BigDecimal.valueOf(
                    Math.round((double) amount * 100.0) / 100.0
            );
            stmt.setBigDecimal(i++, decAmount);
            stmt.setString(i++, baNumber);
            stmt.setString(i++, baBranchCode);
            stmt.setString(i++, baName);
            stmt.setString(i++, baBank);
            stmt.registerOutParameter(i++, java.sql.Types.BOOLEAN);
            stmt.registerOutParameter(i++, java.sql.Types.INTEGER);

            stmt.execute();

            boolean success = stmt.getBoolean(7);
            int txnId = stmt.getInt(8);
            Integer txnObj = stmt.wasNull() ? null : txnId;

            out[0] = success;
            out[1] = txnObj;
            out[2] = null;

        } catch (SQLException e) {
            Log.e("MySQLConnector", "createWithdrawBankAndPendingTransaction failed: " + e.getMessage(), e);
        }
        return out;
    }

    public static Boolean resetPassword(String userEmail, String recoveryCode, String newPassword, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "No valid DB connection.");
            return false;
        }

        boolean resetSuccess;

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL MandelaMoneyDB.ResetPasswordWithRecoveryCode(?, ?, ?, ?)}")) {
            callableStatement.setString(1, userEmail);
            callableStatement.setString(2, recoveryCode);
            callableStatement.setString(3, newPassword);
            callableStatement.registerOutParameter(4, java.sql.Types.BOOLEAN);

            Log.d("MySQLConnector", "Calling ResetPasswordWithRecoveryCode for user: " + userEmail);
            callableStatement.execute();

            resetSuccess = callableStatement.getBoolean(4);
            Log.d("MySQLConnector", "Password reset success: " + resetSuccess);

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'ResetPasswordWithRecoveryCode': " + e.getMessage());
            resetSuccess = false;
        }

        return resetSuccess;
    }



    public static boolean checkUniqueEmail(String userEmail, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot check unique email: No valid database connection.");
            return false;
        }

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL checkUniqueEmail(?)}")) {
            callableStatement.setString(1, userEmail);

            Log.d("MySQLConnector", "Calling checkUniqueEmail for user: " + userEmail);
            boolean hasResultSet = callableStatement.execute();

            if (hasResultSet) {
                try (ResultSet resultSet = callableStatement.getResultSet()) {
                    if (resultSet.next()) {
                        int result = resultSet.getInt(1);
                        if (result == 1) {
                            Log.d("MySQLConnector", "Email is unique.");
                            return true;
                        } else {
                            Log.d("MySQLConnector", "Email is not unique.");
                            return false;
                        }
                    }
                }
            } else {
                Log.d("MySQLConnector", "Stored procedure 'checkUniqueEmail' did not return a ResultSet.");
            }
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'checkUniqueEmail': " + e.getMessage());
        }

        return false;
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

    public static boolean createBusinessAccount(String userEmail, String userPassword, String businessName,
                                                String businessPhoneNumber, String businessVAT, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot create business account: No valid database connection.");
            return false;
        }

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL MandelaMoneyDB.CreateBusiness(?, ?, ?, ?, ?, ?)}")) {
            callableStatement.setString(1, userEmail);
            callableStatement.setString(2, userPassword);
            callableStatement.setString(3, businessName);
            callableStatement.setString(4, businessPhoneNumber);
            callableStatement.setString(5, businessVAT);

            callableStatement.registerOutParameter(6, Types.INTEGER);

            Log.d("MySQLConnector", "Calling CreateBusiness for email: " + userEmail);
            callableStatement.execute();

            int result = callableStatement.getInt(6);

            if (result == 1) {
                Log.d("MySQLConnector", "Business account created successfully for email: " + userEmail);
                showToastOnUI(context, "Business account created successfully!", Toast.LENGTH_SHORT);
                return true;
            } else {
                Log.e("MySQLConnector", "Failed to create business account for email: " + userEmail + ". Email might already exist.");
                showToastOnUI(context, "Failed to create business account. Email might already exist.", Toast.LENGTH_LONG);
                return false;
            }

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'CreateBusiness': " + e.getMessage());
            showToastOnUI(context, "Error creating business account: " + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        }
    }
    public static Integer createTransaction(String toUserEmail, Float transactionAmount, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot create transaction: No valid database connection.");
            return null;
        }

        Integer transactionId = null;

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL MandelaMoneyDB.createTransaction(?, ?, ?)}")) {
            callableStatement.setString(1, toUserEmail);
            callableStatement.setFloat(2, transactionAmount);
            callableStatement.registerOutParameter(3, Types.INTEGER);

            Log.d("MySQLConnector", "Calling createTransaction for toUser: " + toUserEmail);
            callableStatement.execute();

            transactionId = callableStatement.getInt(3);
            Log.d("MySQLConnector", "Transaction created with ID: " + transactionId);
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'createTransaction': " + e.getMessage());
        }

        return transactionId;
    }
    public static List<Transaction> getTransactionHistoryWithFilters(String userEmail, String period, String type, Context context) {
        List<Transaction> transactions = new ArrayList<>();
        Connection conn;

        try {
            conn = getConnection(context);
            if (conn == null) {
                Log.e("MySQLConnector", "No valid connection for getTransactionHistoryWithFilters.");
                return transactions;
            }

            CallableStatement stmt = conn.prepareCall("{call MandelaMoneyDB.getTransactionHistoryWithFilters(?, ?, ?)}");
            stmt.setString(1, userEmail);
            stmt.setString(2, period != null ? period : "All");
            stmt.setString(3, type != null ? type : "All");

            ResultSet rs = stmt.executeQuery();

            transactions = ResultSetParser.parseTransactions(rs, userEmail);

        } catch (SQLException e) {
            Log.e("MySQLConnector", "SQLException in getTransactionHistoryWithFilters: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e("MySQLConnector", "Exception in getTransactionHistoryWithFilters: " + e.getMessage(), e);
        }

        Log.d("MySQLConnector", "Transaction count: " + transactions.size() + 1);
        return transactions;
    }

    public static List<Transaction> getTransactionHistory(String userEmail, Context context) {
        List<Transaction> transactions = new ArrayList<>();
        Connection conn;
        try {
            conn = getConnection(context);
            if (conn == null) {
                Log.e("MySQLConnector", "No valid connection for getTransactionHistory.");
                return transactions;
            }

            CallableStatement stmt = conn.prepareCall("{call MandelaMoneyDB.getUserTransactionHistory(?)}");
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();

            transactions = ResultSetParser.parseTransactions(rs, userEmail);

        } catch (SQLException e) {
            Log.e("MySQLConnector", "SQLException in getTransactionHistory: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e("MySQLConnector", "General Exception in getTransactionHistory: " + e.getMessage(), e);
        }
        Log.d("MySQLConnector", "Transaction count: " + transactions.size() + 1);
        return transactions;
    }

    public static String getTransactionStatus(int transactionId, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot check transaction status: No valid DB connection.");
            return null;
        }

        String status = null;

        try (CallableStatement stmt = currentConnection.prepareCall("{CALL MandelaMoneyDB.getTransactionStatus(?, ?)}")) {
            stmt.setInt(1, transactionId);
            stmt.registerOutParameter(2, Types.VARCHAR);

            stmt.execute();
            status = stmt.getString(2);
            Log.d("MySQLConnector", "Transaction " + transactionId + " status: " + status);

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling getTransactionStatus: " + e.getMessage());
        }

        return status;
    }
    public static void updateTransactionFromUser(Context context, int transactionID, String fromUserEmail) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "No valid DB connection for updateTransactionFromUser");
            return;
        }

        try (CallableStatement stmt = currentConnection.prepareCall("{CALL UpdateTransactionFromUser(?, ?)}")) {
            stmt.setInt(1, transactionID);
            stmt.setString(2, fromUserEmail);

            stmt.execute();

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error updating transaction: " + e.getMessage());
        }
    }

    public static Boolean transactionExists(Context context, int transactionID) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "No valid DB connection for transactionExists.");
            return null;
        }

        try (CallableStatement stmt = currentConnection.prepareCall("{CALL CheckTransactionExists(?, ?)}")) {
            stmt.setInt(1, transactionID);
            stmt.registerOutParameter(2, Types.BOOLEAN);

            stmt.execute();
            boolean exists = stmt.getBoolean(2);
            Log.d("MySQLConnector", "CheckTransactionExists for ID " + transactionID + ": " + exists);

            return exists;

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error checking transaction existence: " + e.getMessage());
            return null;
        }
    }


    public static Transaction getTransactionDetailsFromProcedure(int txnId, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "No valid DB connection for getTransactionDetailsFromProcedure.");
            return null;
        }

        Transaction details = null;

        try (CallableStatement stmt = currentConnection.prepareCall("{CALL MandelaMoneyDB.getTransactionDetails(?)}")) {
            stmt.setInt(1, txnId);

            boolean hasResults = stmt.execute();
            if (hasResults) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        details = new Transaction(
                                rs.getString("fromUser"),
                                rs.getString("toUser"),
                                rs.getFloat("transactionAmount"),
                                rs.getString("formattedDate"),
                                rs.getString("formattedTime")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error fetching transaction details: " + e.getMessage());
        }

        return details;
    }

    public static User getUserDetailsByEmail(String email, Context context) {
        Connection conn = getConnection(context);
        if (conn == null) {
            Log.e("MySQLConnector", "No valid connection for getUserDetailsByEmail.");
            return null;
        }

        User userDetails = null;

        try (CallableStatement cs = conn.prepareCall("{CALL MandelaMoneyDB.getUserDetailsByEmail(?)}")) {
            cs.setString(1, email);

            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    if (Objects.equals(rs.getString("userType"), "student")) {
                        userDetails = new Student(rs.getString("userEmail"));
                        ((Student) userDetails).setStudentFirstName(rs.getString("firstName"));
                        ((Student) userDetails).setStudentLastName(rs.getString("lastName"));
                        ((Student) userDetails).setStudentNumber(rs.getString("userNumber"));
                    } else if (Objects.equals(rs.getString("userType"), "business")) {
                        userDetails = new Business(rs.getString("userEmail"));
                        ((Business) userDetails).setBusinessName(rs.getString("firstName"));
                        ((Business) userDetails).setBusinessVAT(rs.getString("userNumber"));
                    }
                }
            }

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error retrieving user details: " + e.getMessage());
        }

        return userDetails;
    }


    public static boolean hasSufficientFunds(String fromUserEmail, int transactionId, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot check funds: No valid DB connection.");
            return false;
        }
        boolean isSufficient = false;

        try (CallableStatement stmt = currentConnection.prepareCall("{CALL MandelaMoneyDB.sufficientFunds(?, ?, ?)}")) {
            stmt.setString(1, fromUserEmail);
            stmt.setInt(2, transactionId);
            stmt.registerOutParameter(3, Types.BOOLEAN);

            stmt.execute();
            isSufficient = stmt.getBoolean(3);

            Log.d("MySQLConnector", "Sufficient funds? " + isSufficient);
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling sufficientFunds: " + e.getMessage());
        }

        return isSufficient;
    }

    public static boolean confirmTransaction(String fromUserEmail, int txnId, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot confirm transaction: No valid DB connection.");
            return false;
        }

        boolean txnSuccess = false;
        Log.i("JHGG", "HERE");
        try (CallableStatement stmt = currentConnection.prepareCall("{CALL MandelaMoneyDB.confirmTransaction(?, ?, ?)}")) {
            stmt.setString(1, fromUserEmail);
            stmt.setInt(2, txnId);
            stmt.registerOutParameter(3, Types.BOOLEAN);
            Log.i("JHGG", "HERE2");
            stmt.execute();
            txnSuccess = stmt.getBoolean(3);
            Log.d("MySQLConnector", "Transaction confirmed? " + txnSuccess);
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling confirmTransaction: " + e.getMessage());
        }

        return txnSuccess;
    }

    public static void updateTransactionStatus(int transactionId, String newStatus, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot update transaction status: No valid DB connection.");
            return;
        }

        try (CallableStatement stmt = currentConnection.prepareCall("{CALL MandelaMoneyDB.updateTransactionStatus(?, ?)}")) {
            stmt.setInt(1, transactionId);
            stmt.setString(2, newStatus);

            stmt.execute();
            Log.d("MySQLConnector", "Transaction status updated to: " + newStatus + " for ID: " + transactionId);

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error updating transaction status: " + e.getMessage());
        }
    }


    public static boolean createStudentAccount(String userEmail, String userPassword, String studentFirstName,
                                               String studentLastName, String studentNumber, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot create student account: No valid database connection.");
            return false;
        }

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL MandelaMoneyDB.CreateStudent(?, ?, ?, ?, ?, ?)}")) {
            callableStatement.setString(1, userEmail);
            callableStatement.setString(2, userPassword);
            callableStatement.setString(3, studentFirstName);
            callableStatement.setString(4, studentLastName);
            callableStatement.setString(5, studentNumber);

            callableStatement.registerOutParameter(6, Types.INTEGER);

            Log.d("MySQLConnector", "Calling CreateStudent for email: " + userEmail);
            callableStatement.execute();

            int result = callableStatement.getInt(6);

            if (result == 1) {
                Log.d("MySQLConnector", "Student account created successfully for email: " + userEmail);
                showToastOnUI(context, "Student account created successfully!", Toast.LENGTH_SHORT);
                return true;
            } else {
                Log.e("MySQLConnector", "Failed to create student account for email: " + userEmail + ". Email might already exist.");
                showToastOnUI(context, "Failed to create student account. Email might already exist.", Toast.LENGTH_LONG);
                return false;
            }

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'CreateStudent': " + e.getMessage());
            showToastOnUI(context, "Error creating student account: " + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        }
    }

    public static float getUserBalance(String email, Context context) {
        Connection currentConnection = getConnection(context);
        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot fetch balance: No valid DB connection.");
            return 0.0F;
        }

        float balance = 0.0F;

        try (CallableStatement stmt = currentConnection.prepareCall("{CALL MandelaMoneyDB.getUserBalance(?, ?)}")) {
            stmt.setString(1, email);
            stmt.registerOutParameter(2, Types.FLOAT);

            stmt.execute();
            balance = stmt.getFloat(2);

            Log.d("MySQLConnector", "Fetched balance for " + email + ": " + balance);

        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling getUserBalance: " + e.getMessage());
        }

        return balance;
    }
    public static Map<String, String> getDisplayNamesForEmails(Set<String> emails, Context context) {
        Map<String, String> emailToDisplayName = new HashMap<>();

        Connection conn = getConnection(context);
        if (conn == null) {
            Log.e("MySQLConnector", "No valid connection for getDisplayNamesForEmails.");
            return emailToDisplayName;
        }

        for (String email : emails) {
            try (CallableStatement stmt = conn.prepareCall("{CALL MandelaMoneyDB.getUserDisplayNameByEmail(?)}")) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String displayName = rs.getString("displayName");
                        emailToDisplayName.put(email, displayName);
                    }
                }
            } catch (SQLException e) {
                Log.e("MySQLConnector", "Error fetching display name for: " + email + " → " + e.getMessage());
            }
        }

        return emailToDisplayName;
    }

    public static Student updateStudentDetails(
            String userEmail,
            String userPassword,
            String newEmail,
            String newFirstName,
            String newLastName,
            String newStudentNumber,
            Context context
    ) {
        Connection currentConnection = getConnection(context);
        Student updatedStudent = null;

        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot update student details: No valid database connection.");
            return null;
        }

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL UpdateStudentDetails(?, ?, ?, ?, ?, ?, ?)}")) {
            // Set the input parameters
            callableStatement.setString(1, userEmail);
            callableStatement.setString(2, userPassword);
            callableStatement.setString(3, newEmail);
            callableStatement.setString(4, newFirstName);
            callableStatement.setString(5, newLastName);
            callableStatement.setString(6, newStudentNumber);

            // Register output parameter
            callableStatement.registerOutParameter(7, Types.INTEGER);

            Log.d("MySQLConnector", "Calling UpdateStudentDetails for user: " + userEmail);

            // Execute the stored procedure
            callableStatement.execute();

            // Retrieve the result of the output parameter
            int result = callableStatement.getInt(7);
            if (result == 1) {
                Log.d("MySQLConnector", "Student details updated successfully.");
                // Create and return the updated Student object
                updatedStudent = new Student(newEmail);
                updatedStudent.setStudentFirstName(newFirstName);
                updatedStudent.setStudentLastName(newLastName);
                updatedStudent.setStudentNumber(newStudentNumber);
            } else {
                Log.d("MySQLConnector", "Failed to update student details.");
            }
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'UpdateStudentDetails': " + e.getMessage());
        }

        return updatedStudent;
    }

    public static Business updateBusinessDetails(
            String userEmail,
            String userPassword,
            String newEmail,
            String newBusinessName,
            String newVAT,
            String newPhoneNumber,
            Context context
    ) {
        Connection currentConnection = getConnection(context);
        Business updatedBusiness = null;

        if (currentConnection == null) {
            Log.e("MySQLConnector", "Cannot update business details: No valid database connection.");
            return null;
        }

        try (CallableStatement callableStatement = currentConnection.prepareCall("{CALL UpdateBusinessDetails(?, ?, ?, ?, ?, ?, ?)}")) {
            // Set the input parameters
            callableStatement.setString(1, userEmail);
            callableStatement.setString(2, userPassword);
            callableStatement.setString(3, newEmail);
            callableStatement.setString(4, newBusinessName);
            callableStatement.setString(5, newVAT);
            callableStatement.setString(6, newPhoneNumber);

            // Register output parameter
            callableStatement.registerOutParameter(7, Types.INTEGER);

            Log.d("MySQLConnector", "Calling UpdateBusinessDetails for user: " + userEmail);

            // Execute the stored procedure
            callableStatement.execute();

            // Retrieve the result of the output parameter
            int result = callableStatement.getInt(7);
            if (result == 1) {
                Log.d("MySQLConnector", "Business details updated successfully.");
                // Create and return the updated Business object
                updatedBusiness = new Business(newEmail);
                updatedBusiness.setBusinessName(newBusinessName);
                updatedBusiness.setBusinessPhoneNumber(newPhoneNumber);
                updatedBusiness.setBusinessVAT(newVAT);
            } else {
                Log.d("MySQLConnector", "Failed to update business details.");
            }
        } catch (SQLException e) {
            Log.e("MySQLConnector", "Error calling stored procedure 'UpdateBusinessDetails': " + e.getMessage());
        }

        return updatedBusiness;
    }




}