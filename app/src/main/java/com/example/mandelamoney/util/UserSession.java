package com.example.mandelamoney.util;

import com.example.mandelamoney.model.data.User;

/// this is the currently logged in used
public class UserSession {
    private static User currentUser;

    public static User getUser() {
        return currentUser;
    }

    public static void setUser(User user) {
        currentUser = user;
    }

    public static void clearSession() {
        currentUser = null;
    }
}
