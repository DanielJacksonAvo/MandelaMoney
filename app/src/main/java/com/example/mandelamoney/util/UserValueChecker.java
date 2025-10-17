package com.example.mandelamoney.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserValueChecker {
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean checkPasswordLength(String userPassword) {
        return userPassword.length() >= 8;
    }

    public static boolean checkPasswordMatch(String userPassword, String userPasswordReenter) {
        return Objects.equals(userPassword, userPasswordReenter);
    }

    public static boolean checkEmpty(String s) {
        return s == null || s.isEmpty();
    }


    public static boolean isValidStudentNumber(String studentNumber) {
        // FIX: Check for both null and an empty string at the beginning.
        if (studentNumber == null || studentNumber.isEmpty()) {
            return false;
        }

        // This block is now safe because we know the string is not empty.
        if (studentNumber.charAt(0) != 's') {
            studentNumber = "s" + studentNumber;
        }

        String regex = "^s\\d{9}$";
        return studentNumber.matches(regex);
    }

    public static boolean isValidVatNumber(String vatNumber) {
        return vatNumber != null && vatNumber.matches("^\\d{10}$");
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^0\\d{9}$");
    }
}
