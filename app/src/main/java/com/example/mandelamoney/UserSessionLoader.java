package com.example.mandelamoney;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionLoader {

    public static void tryRestoreSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String email = prefs.getString("userEmail", null);
        String type = prefs.getString("userType", null);

        if (email != null && type != null) {
            UserDetails details = MySQLConnector.getUserDetailsByEmail(email, context);
            double balance = MySQLConnector.getUserBalance(email, context); // You may need to implement this

            if (details != null) {
                User user = null;
                if (type.equals("student")) {
                    user = new Student(email, "", balance,
                            details.getFirstName(), details.getLastName(), details.getNumber());
                } else if (type.equals("business")) {
                    user = new Business(email, "", balance,
                            details.getFirstName(), details.getNumber(), "VAT123");
                }

                UserSession.setUser(user);
            }
        }
    }
}

