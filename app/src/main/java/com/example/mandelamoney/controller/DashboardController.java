package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Handler;
import com.example.mandelamoney.R;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.TransactionDetails;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.IDashboardView;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;
import com.example.mandelamoney.view.Iface.ITransactionHistoryView;
import com.example.mandelamoney.view.activity.MakePaymentScanQrActivity;
import com.example.mandelamoney.view.activity.RequestPaymentEnterAmountActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DashboardController {
    private final IDashboardView view;
    private final Context context;
    private final User user;

    private int currentFragment = -1; //0 - home, 1 - lock, 2 - settings, 3 - profile

    public DashboardHomeController DashboardHomeController;
    public TransactionHistoryController TransactionHistoryController;


    public DashboardController(Context context, IDashboardView view) {
        this.context = context;
        this.view = view;
        this.user = UserSession.getUser();
    }


    public void handleHome() {
        currentFragment = 0;
        view.displayHome();
        manageControllers();

    }

    public void handleLock() {
        currentFragment = 1;
        view.displayLock();
        manageControllers();
    }

    public void handleSettings() {
        currentFragment = 2;
        view.displaySettings();
        manageControllers();
    }

    public void handleProfile() {
        currentFragment = 3;
        view.displayProfile();
        manageControllers();
    }
    public void handleViewTransactionHistory() {
        view.displayTransactionHistoryScreen();
        manageControllers();
    }

    private void manageControllers() {
        if (currentFragment == 0) {
            DashboardHomeController.startPolling();
        } else {
            DashboardHomeController.stopPolling();
        }

    }

    public void handleSelection(int itemId) {
        if (itemId == R.id.nav_lock) {
            handleLock();
        } else if (itemId == R.id.nav_settings) {
            handleSettings();
        } else if (itemId == R.id.nav_profile) {
            handleProfile();
        } else {
            handleHome();
        }
    }

    public void createDashboardHomeController(IHomeDashboardView view) {
        DashboardHomeController = new DashboardHomeController(view);
    }

    public void createTransactionHistoryController(ITransactionHistoryView view) {
        TransactionHistoryController = new TransactionHistoryController(view);
    }

    public class DashboardHomeController {
        private final IHomeDashboardView view;
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        private ScheduledFuture<?> pollingHandle;

        public DashboardHomeController(IHomeDashboardView view) {
            this.view = view;
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
                double previousBalance = user.getUserBalance();
                double updatedBalance = MySQLConnector.getUserBalance(user.getUserEmail(), context);

                if (updatedBalance != previousBalance) {
                    user.setUserBalance(updatedBalance);
                    mainThreadHandler.post(() -> view.displayBalance(updatedBalance));
                    TransactionHistoryController.refreshAndDisplayTransactions();
                }
            }
        }


//    public void handleLoadTransactionsToUI() {
//        pullSQLTransaction();
//    }

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

    private class DashboardLockController {

    }

    private class DashboardSettingsController {

    }

    private class DashboardProfileController {

    }
    public class TransactionHistoryController {
        private ITransactionHistoryView transactionHistoryView;
        private final User user;
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());


        public TransactionHistoryController(ITransactionHistoryView transactionHistoryView) {
            this.user = UserSession.getUser();
            this.transactionHistoryView = transactionHistoryView;
        }



        public void handleLoadUserToUI() {
            if (user instanceof Student) {
                String fullname = ((Student) user).getStudentFirstName() + " " + ((Student) user).getStudentLastName();
                transactionHistoryView.displayUserName(fullname);
            } else if (user instanceof Business) {
                transactionHistoryView.displayUserName(((Business) user).getBusinessName());
            }
        }


        public List<TransactionDetails> formatTransactionHistory(List<TransactionDetails> transactionList, Context context) {
            String currentUserEmail = UserSession.getUser().getUserEmail();
            Set<String> emailsToLookup = new HashSet<>();
            for (TransactionDetails tx : transactionList) {
                String from = tx.getFromUser();
                String to = tx.getToUser();
                if (from.equals(currentUserEmail)) {
                    emailsToLookup.add(to);
                }
                if (to.equals(currentUserEmail)) {
                    emailsToLookup.add(from);
                }
            }

            Map<String, String> emailToDisplayName = MySQLConnector.getDisplayNamesForEmails(emailsToLookup, context);

            for (TransactionDetails tx : transactionList) {
                String from = tx.getFromUser();
                String to = tx.getToUser();

                if (from.equals(currentUserEmail)) {
                    tx.setFromUser(emailToDisplayName.getOrDefault(to, to));

                }

                if (to.equals(currentUserEmail)) {
                    tx.setToUser(emailToDisplayName.getOrDefault(from, from));
                    tx.setAmount(tx.getAmount()*-1);
                }
            }

            return transactionList;
        }
        public void refreshAndDisplayTransactions() {
            new Thread(() -> {
                String email = UserSession.getUser().getUserEmail();
                List<TransactionDetails> rawList = MySQLConnector.getTransactionHistory(email, context);
                List<TransactionDetails> formattedList = formatTransactionHistory(rawList, context);
                UserSession.setCachedTransactionHistory(formattedList);

                mainThreadHandler.post(() -> {
                    if (transactionHistoryView != null) {
                        transactionHistoryView.updateData(formattedList);
                    }
                });
            }).start();
        }

        public void queryWithFilters(String searchQuery, String period, String type) {
            new Thread(() -> {
                String userEmail = UserSession.getUser().getUserEmail();
                List<TransactionDetails> fullList = MySQLConnector.getTransactionHistory(userEmail, context);
                List<TransactionDetails> filtered = new ArrayList<>();

                for (TransactionDetails txn : fullList) {
                    boolean matches = true;

                    if (searchQuery != null && !searchQuery.isEmpty()) {
                        String targetName = txn.getFromUser() + " " + txn.getToUser();
                        if (!targetName.toLowerCase().contains(searchQuery.toLowerCase())) {
                            matches = false;
                        }
                    }

                    // Add future logic for period/type here

                    if (matches) {
                        filtered.add(txn);
                    }
                }

                List<TransactionDetails> formatted = formatTransactionHistory(filtered, context);
                mainThreadHandler.post(() -> transactionHistoryView.updateData(formatted));
            }).start();
        }


    }


}
