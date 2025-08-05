package com.example.mandelamoney.util;

import android.content.Context;

import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PaymentManager {
    public Transaction makePayment(){
        return null;
    }

    public void getTransaction(int id, Context context, Consumer<Transaction> onSuccess, Runnable onError) {
        new Thread(()-> {
            if (Boolean.TRUE.equals(MySQLConnector.transactionExists(context, id))) {
                MySQLConnector.updateTransactionFromUser(context, id, ((User)UserSession.getUser()).getUserEmail());
                Transaction tx = MySQLConnector.getTransactionDetailsFromProcedure(id, context);
                User toUser = MySQLConnector.getUserDetailsByEmail(tx.getToUser() ,context);
                User fromUser = MySQLConnector.getUserDetailsByEmail(tx.getFromUser(), context);
                tx.setToUserObj(toUser);
                tx.setFromUserObj(fromUser);
                runOnMainThread(context, () -> onSuccess.accept(tx));
            }
            else {

            }
        }).start();

    }

    public Transaction getTransactionStatus() {
        return null;
    }

    private static void runOnMainThread(Context context, Runnable runnable) {
        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
        handler.post(runnable);
    }

}
