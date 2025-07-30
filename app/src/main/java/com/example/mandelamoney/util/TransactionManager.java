package com.example.mandelamoney.util;

import android.content.Context;
import android.util.Log;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.TransactionDetails;
import com.example.mandelamoney.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionManager {
    public static List<TransactionDetails> formatTransactionHistory(List<TransactionDetails> transactionList, Context context) {
        Log.d("THController", "formatTransactionHistory(): received " + transactionList.size() + " transactions");

        User user = UserSession.getUser();
        if (user == null) {
            Log.e("THController", "User is null in formatTransactionHistory()");
            return transactionList;  // Return as-is to avoid crash, or you can handle this scenario as needed
        }
        String currentUserEmail = user.getUserEmail();
        Set<String> emailsToLookup = new HashSet<>();

        // Collect emails to lookup
        for (TransactionDetails tx : transactionList) {
            Log.d("THController", "Before format: " + tx.toString());

            if (tx.isSelfTransaction()) {
                Log.d("THController", "Self transaction detected for " + currentUserEmail + ", fromUser=" + tx.getFromUser() + ", toUser=" + tx.getToUser());
            } else {
                if (tx.getFromUser().equals(currentUserEmail)) {
                    emailsToLookup.add(tx.getToUser());
                    Log.d("THController","Adding toUser " + tx.getToUser() + " to lookup (fromUser is current user)");
                }
                if (tx.getToUser().equals(currentUserEmail)) {
                    emailsToLookup.add(tx.getFromUser());
                    Log.d("THController","Adding fromUser " + tx.getFromUser() + " to lookup (toUser is current user)");
                }
            }
        }

        Log.d("THController", "Emails to lookup: " + emailsToLookup);

        // Get display names for other users
        Map<String, String> emailToDisplayName = MySQLConnector.getDisplayNamesForEmails(emailsToLookup, context);

        // Format each transaction
        for (TransactionDetails tx : transactionList) {
            if (tx.isSelfTransaction()) {
                setSelfTransactionName(tx);
            } else {
                if (tx.getFromUser().equals(currentUserEmail)) {
                    String displayName = emailToDisplayName.getOrDefault(tx.getToUser(), tx.getToUser());
                    Log.d("THController", "Outgoing: replacing " + tx.getToUser() + " with " + displayName);
                    tx.setDisplayName(displayName);
                    tx.setAmount(tx.getAmount() * -1);
                } else if (tx.getToUser().equals(currentUserEmail)) {
                    String displayName = emailToDisplayName.getOrDefault(tx.getFromUser(), tx.getFromUser());
                    Log.d("THController", "Incoming: replacing " + tx.getFromUser() + " with " + displayName);
                    tx.setDisplayName(displayName);

                }

            }
        }
        return transactionList;
    }

    private static void setSelfTransactionName(TransactionDetails tx) {
        User user = UserSession.getUser();
        if (user instanceof Student) {
            String fullname = ((Student) user).getStudentFirstName() + " " + ((Student) user).getStudentLastName();
            tx.setDisplayName(fullname);
        } else if (user instanceof Business) {
            String name = ((Business) user).getBusinessName();
            tx.setDisplayName(name);
        }
        tx.setSelfTransaction(true);
        Log.d("THController", "Setting self-transaction name with from user: "+tx.getFromUser() + "to user: "+tx.getToUser());
    }
}
