package com.example.mandelamoney.util;

import android.content.Context;

import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;

import java.util.Objects;
import java.util.function.Consumer;

public class PaymentManager {

    public static void processTransaction(int id, Context context, Consumer<Transaction> onSuccess, Consumer<String> onError) {
        new Thread(()-> {
            try {
                if (Boolean.TRUE.equals(MySQLConnector.transactionExists(context, id))) {
                    MySQLConnector.updateTransactionFromUser(context, id, ((User)UserSession.getUser()).getUserEmail());
                    Transaction tx = MySQLConnector.getTransactionDetailsFromProcedure(id, context);
                    tx.setId(id);
                    User toUser = MySQLConnector.getUserDetailsByEmail(tx.getToUser() ,context);
                    User fromUser = MySQLConnector.getUserDetailsByEmail(tx.getFromUser(), context);
                    tx.setToUserObj(toUser);
                    tx.setFromUserObj(fromUser);
                    runOnMainThread(context, () -> onSuccess.accept(tx));
                }
                else {
                    String error = "Invalid Transaction ID";
                    runOnMainThread(context,() -> onError.accept(error));
                }
            } catch (Exception e) {
                String error = "Error Processing Transaction";
                runOnMainThread(context, () -> onError.accept(error));
            }

        }).start();

    }

    public static void confirmTransaction(Transaction transaction, Context context, Runnable onSuccess, Consumer<String> onFailure) {
        new Thread(() -> {
            try {
                MySQLConnector.hasSufficientFunds(transaction.getFromUserObj().getUserEmail(), Integer.parseInt(transaction.getId()), context);
                if (MySQLConnector.hasSufficientFunds(transaction.getFromUserObj().getUserEmail(), Integer.parseInt(transaction.getId()), context)) {
                    if (Objects.equals(transaction.getFromUserObj().getUserEmail(), UserSession.getUser().getUserEmail())) {
                        if (MySQLConnector.confirmTransaction(transaction.getFromUserObj().getUserEmail(), Integer.parseInt(transaction.getId()), context)) {
                            runOnMainThread(context, onSuccess);
                        } else {
                            String error = "Transaction Failed or Reversed";
                            runOnMainThread(context, () -> onFailure.accept(error));
                        }
                    } else {
                        String error = "User Session Expired";
                        runOnMainThread(context, () -> onFailure.accept(error));
                    }
                } else {
                    String error = "Insufficient Funds";
                    runOnMainThread(context, () -> onFailure.accept(error));
                }
            } catch (Exception e) {
                String error = "Transaction Failed or Reversed";
                runOnMainThread(context, () -> onFailure.accept(error));
                throw new RuntimeException(e);
            }
        }).start();

    }

    public static void createTransaction(Float amount, Context context, Consumer<Transaction> onSuccess, Consumer<String> onFailure) {
        new Thread(() -> {
            try {
                Integer id = MySQLConnector.createTransaction(UserSession.getUser().getUserEmail(), amount, context);
                if (id == null) {
                    String error = "Failed To Create Transaction";
                    runOnMainThread(context, () -> onFailure.accept(error));
                } else {
                    Transaction transaction = MySQLConnector.getTransactionDetailsFromProcedure(id, context);
                    if (transaction == null) {
                        String error = "Failed To Create Transaction";
                        runOnMainThread(context, () -> onFailure.accept(error));
                    } else {
                        transaction.setId(id);
                        runOnMainThread(context, () -> onSuccess.accept(transaction));
                    }
                }
            } catch (Exception e) {
                String error = "Failed To Create Transaction";
                runOnMainThread(context, () -> onFailure.accept(error));
            }

        }).start();



    }

    private static void runOnMainThread(Context context, Runnable runnable) {
        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
        handler.post(runnable);
    }

}
