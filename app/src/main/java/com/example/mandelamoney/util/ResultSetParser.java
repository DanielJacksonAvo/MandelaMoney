package com.example.mandelamoney.util;

import android.util.Log;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ResultSetParser {
    private static final int EXPECTED_COLUMN_COUNT = 5;

    public static User parseValidateEmailPassword(ResultSet resultSet, String userEmail, String userPassword) throws SQLException {
        User user = null;

        if (resultSet.next()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            if (columnCount != EXPECTED_COLUMN_COUNT) {
                return null;
            }

            int userType = resultSet.getInt(1);
            float userBalance = resultSet.getFloat(2);
            String col3 = resultSet.getString(3);
            String col4 = resultSet.getString(4);
            String col5 = resultSet.getString(5);

            if (resultSet.next()) {
                return null;
            } else {
                if (userType == 1) {
                    user = new Student(userEmail);
                    user.setUserPassword(userPassword);
                    user.setUserBalance(userBalance);
                    ((Student) user).setStudentFirstName(col3);
                    ((Student) user).setStudentLastName(col4);
                    ((Student) user).setStudentNumber(col5);
                } else if (userType == 2) {
                    user = new Business(userEmail);
                    user.setUserPassword(userPassword);
                    user.setUserBalance(userBalance);
                    ((Business) user).setBusinessName(col3);
                    ((Business) user).setBusinessPhoneNumber(col4);
                    ((Business) user).setBusinessVAT(col5);
                }
            }
        }
        return user;
    }


    public static List<Transaction> parseTransactions(ResultSet rs, String userEmail) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        // 1. Define the formatter that matches your SQL output
        //    Format: "25 October 2025" + " " + "14:21"
        SimpleDateFormat uctInputFormatter = new SimpleDateFormat("d MMMM yyyy HH:mm", Locale.ENGLISH);
        // Tell this formatter that the input strings it's parsing are in UTC
        uctInputFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        // 2. Define your desired output formatters
        SimpleDateFormat localDateFormatter = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
        SimpleDateFormat localTimeFormatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        // Output formatters automatically use the device's default time zone
        // (TimeZone.getDefault()), so no setTimeZone call is needed for them.

        while (rs.next()) {
            String from = rs.getString("fromUser");
            String to = rs.getString("toUser");
            float amount = rs.getFloat("transactionAmount");

            // Get the original UCT date and time strings from SQL
            String uctDate = rs.getString("date");
            String uctTime = rs.getString("time");

            String localDate = uctDate; // Default to original values in case parsing fails
            String localTime = uctTime;

            try {
                // 3. Combine and parse the UCT string into a Date object
                String uctDateTimeString = uctDate + " " + uctTime;
                Date uctAsDateObject = uctInputFormatter.parse(uctDateTimeString);

                // 4. Re-format that Date object into local date and time strings
                //    Java's formatter handles the time zone conversion automatically.
                if (uctAsDateObject != null) {
                    localDate = localDateFormatter.format(uctAsDateObject);
                    localTime = localTimeFormatter.format(uctAsDateObject);
                }

            } catch (ParseException e) {
                // If parsing fails, just use the raw UCT strings from the DB
                Log.e("MySQLConnector", "Failed to parse UCT timestamp: " + uctDate + " " + uctTime, e);
            }

            Log.d("MySQLConnector", "Transaction: from=" + from + ", to=" + to + ", amount=" + amount + ", date=" + localDate + ", time=" + localTime);

            Transaction tx;
            if (from.equals(userEmail) && to.equals(userEmail)) {
                // Use the new local date and time strings
                tx = new Transaction(from, to, amount, localDate, localTime);
                tx.setSelfTransaction(true);
                transactions.add(tx);
            } else {
                // Use the new local date and time strings
                tx = new Transaction(from, to, amount, localDate, localTime);
                tx.setSelfTransaction(false);
                transactions.add(tx);
            }
        }
        return transactions;
    }
}
