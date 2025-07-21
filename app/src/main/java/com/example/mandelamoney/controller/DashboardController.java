package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Handler;

import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.IDashboardView;
import com.example.mandelamoney.view.activity.MakePaymentScanQrActivity;
import com.example.mandelamoney.view.activity.RequestPaymentEnterAmountActivity;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DashboardController {
    private final IDashboardView view;
    private final Context context;
    private final User user;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private ScheduledFuture<?> pollingHandle;


    public DashboardController(Context context, IDashboardView view) {
        this.context = context;
        this.view = view;
        this.user = UserSession.getUser();
    }

    public void handleLoadUserToUI() {
        view.displayBalance(user.getUserBalance());
        if (user instanceof Student) {
            String fullname = ((Student) user).getStudentFirstName() + " " + ((Student) user).getStudentLastName();
            view.displayUserName(fullname);
        } else if (user instanceof Business) {
            view.displayUserName(((Business) user).getBusinessName());
        }
        startPolling();
    }

    public void handleBalanceRefresh() {
        if (user != null) {
            double updatedBalance = MySQLConnector.getUserBalance(user.getUserEmail(), context);
            user.setUserBalance(updatedBalance);
            mainThreadHandler.post(() -> view.displayBalance(updatedBalance));
        }
    }

    public void handleLoadTransactionsToUI() {
        pullSQLTransaction();
    }

    public void handleMakePayment() {
        DataShare.send(this);
        stopPolling();
        Intent intent = new Intent(context, MakePaymentScanQrActivity.class);
        context.startActivity(intent);
    }

    public void handleRequestPayment() {
        stopPolling();
        Intent intent = new Intent(context, RequestPaymentEnterAmountActivity.class);
        context.startActivity(intent);
    }

    private void pullSQLTransaction() {
        ArrayList<Transaction> transactionList = new ArrayList<>();
        // TODO: SQL logic to fill transactionList
    }

    public void startPolling() {
        if (pollingHandle != null && !pollingHandle.isDone()) {
            return;
        }

        Runnable statusChecker = () -> {
            try {
                handleBalanceRefresh();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        pollingHandle = scheduler.scheduleWithFixedDelay(statusChecker, 0, 5, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        if (pollingHandle != null) {
            pollingHandle.cancel(true);
            pollingHandle = null;
        }
    }

    public void cleanup() {
        stopPolling();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException ie) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        mainThreadHandler.removeCallbacksAndMessages(null);
    }
}
