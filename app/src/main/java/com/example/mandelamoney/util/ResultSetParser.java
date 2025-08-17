package com.example.mandelamoney.util;

import android.util.Log;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            double userBalance = resultSet.getDouble(2);
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
        while (rs.next()) {
            String from = rs.getString("fromUser");
            String to = rs.getString("toUser");
            float amount = rs.getFloat("transactionAmount");
            String date = rs.getString("date");
            String time = rs.getString("time");

            Log.d("MySQLConnector", "Transaction: from=" + from + ", to=" + to + ", amount=" + amount + ", date=" + date + ", time=" + time);
            Transaction tx;
            if(from.equals(userEmail)&&to.equals(userEmail)){
                tx = new Transaction(from, to, amount, date, time);
                tx.setSelfTransaction(true);
                transactions.add(tx);
            } else {
                tx = new Transaction(from, to, amount, date, time);
                tx.setSelfTransaction(false);
                transactions.add(tx);
            }
        }
        return transactions;
    }
}
