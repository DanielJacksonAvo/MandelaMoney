package com.example.mandelamoney;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

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
                    String firstName = col3;
                    String lastName = col4;
                    String studentNumber = col5;
                    user = new Student(userEmail, userPassword, userBalance, firstName, lastName, studentNumber);
                } else if (userType == 0) {
                    String businessName = col3;
                    String businessPhoneNumber = col4;
                    String businessVAT = col5;
                    user = new Business(userEmail, userPassword, userBalance, businessName, businessPhoneNumber, businessVAT);
                }
            }
        }
        return user;
    }

    public static void main(String[] args) {
    }
}
