package com.example.mandelamoney;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetParser {
    public static User parseValidateEmailPassword(ResultSet resultSet, String userEmail, String userPassword) throws SQLException {
        if (resultSet.next()) {
            int userType = resultSet.getInt(1);
            double userBalance = resultSet.getDouble(2);

            if (userType == 1) {
                String firstName = resultSet.getString(3);
                String lastName = resultSet.getString(4);
                String studentNumber = resultSet.getString(5);
                return new Student(userEmail, userPassword, userBalance, firstName, lastName, studentNumber);
            } else if (userType == 0) {
                String businessName = resultSet.getString(3);
                String businessPhoneNumber = resultSet.getString(4);
                String businessVAT = resultSet.getString(5);
                return new Business(userEmail, userPassword, userBalance, businessName, businessPhoneNumber, businessVAT);
            }
        }
        return null;
    }


    public static void main(String[] args) {
    }
}
