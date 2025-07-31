package com.example.mandelamoney.util;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;

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

    public static void main(String[] args) {
    }
}
