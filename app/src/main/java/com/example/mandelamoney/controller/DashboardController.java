package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DashboardController {
    private final IDashboardView view;
    private final Context context;
    private final User user;

    private int currentFragment = 0; //0 - home, 1 - lock, 2 - settings, 3 - profile

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
        view.displayTabletTransactionHistoryScreen();
        manageControllers();
    }

    public void handleLoadUserToUITablet() {
        view.displayUserNameTablet(DashboardHomeController.getUserName());
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
            try {
                view.displayBalance(user.getUserBalance());
                view.displayUserName(getUserName());
                startPolling();
                refreshAndDisplayTransactions();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        public String getUserName() {
            if (user instanceof Student) {
                return ((Student) user).getStudentFirstName() + " " + ((Student) user).getStudentLastName();
            } else if (user instanceof Business) {
                return ((Business) user).getBusinessName();
            }
            return null;
        }

        public void handleBalanceRefresh() {
              if (user != null) {
                  double previousBalance = user.getUserBalance();
                  double updatedBalance = MySQLConnector.getUserBalance(user.getUserEmail(), context);
                  if (updatedBalance != previousBalance) {
                      user.setUserBalance(updatedBalance);
                      mainThreadHandler.post(() -> view.displayBalance(updatedBalance));
                      refreshAndDisplayTransactions();
                  }
              }
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

            pollingHandle = scheduler.scheduleWithFixedDelay(statusChecker, 0, 3, TimeUnit.SECONDS);
        }

        public void stopPolling() {
            if (pollingHandle != null) {
                pollingHandle.cancel(true);
                pollingHandle = null;
                cleanup();
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


        public void refreshAndDisplayTransactions() {
            new Thread(() -> {
                String email = UserSession.getUser().getUserEmail();
                List<TransactionDetails> rawList = MySQLConnector.getTransactionHistoryWithFilters(email, "Last Week", "All", context);
                List<TransactionDetails> formattedList = TransactionManager.formatTransactionHistory(rawList, context);
                UserSession.setCachedTransactionHistory(formattedList);

                mainThreadHandler.post(() -> {
                    if (view != null) {
                        view.displayTransactions(formattedList);
                    }
                });
            }).start();
        }
    }



    private class DashboardLockController {

    }

    private class DashboardSettingsController {

    }

    private class DashboardProfileController {

    }
    public class TransactionHistoryController {

        private final ITransactionHistoryView transactionHistoryView;
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        public TransactionHistoryController(
                ITransactionHistoryView transactionHistoryView
        ) {
            this.transactionHistoryView = transactionHistoryView;
        }

        public void handleLoadUserToUI() {
            User user = UserSession.getUser();
            if (user instanceof Student) {
                String fullname = ((Student) user).getStudentFirstName() + " " + ((Student) user).getStudentLastName();
                transactionHistoryView.displayUserName(fullname);
            } else if (user instanceof Business) {
                transactionHistoryView.displayUserName(((Business) user).getBusinessName());
            }
        }
        public void loadTransactions(@Nullable String searchQuery,
                                     @Nullable String period,
                                     @Nullable String type) {
            new Thread(() -> {
                String userEmail = UserSession.getUser().getUserEmail();
                Log.d("THController", "loadTransactions() called. Query=" + searchQuery + " Period=" + period + " Type=" + type + " User=" + userEmail);

                boolean periodAll = isAll(period);
                boolean typeAll   = isAll(type);

                List<TransactionDetails> rawList;
                if (periodAll && typeAll) {
                    rawList = MySQLConnector.getTransactionHistory(userEmail, context);
                    Log.d("THController", "Fetched " + rawList.size() + " transactions using getTransactionHistory()");
                } else {
                    String periodArg = periodAll ? null : period;
                    String typeArg   = typeAll   ? null : type;

                    rawList = MySQLConnector.getTransactionHistoryWithFilters(
                            userEmail, periodArg, typeArg, context
                    );
                    Log.d("THController", "Fetched " + rawList.size() + " transactions using getTransactionHistoryWithFilters()");
                }

                // Format
                Log.d("THController", "Formatting transactions...");
                List<TransactionDetails> formattedList = TransactionManager.formatTransactionHistory(rawList, context);
                Log.d("THController", "Formatting complete. Total: " + formattedList.size());

                // Apply search
                // Apply search by displayName
                if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                    Log.d("THController", "Applying search: " + searchQuery);
                    String query = searchQuery.trim().toLowerCase();

                    formattedList = formattedList.stream()
                            .filter(txn -> {
                                String display = txn.getDisplayName() != null ? txn.getDisplayName().toLowerCase() : "";
                                boolean match = display.contains(query);
                                if (match) {
                                    Log.d("THController", "Search match: " + txn);
                                }
                                return match;
                            })
                            .collect(Collectors.toList());

                    Log.d("THController", "Search filter done. Remaining: " + formattedList.size());
                }

                // Cache
                UserSession.setCachedTransactionHistory(formattedList);
                List<TransactionDetails> finalFormattedList = formattedList;
                mainThreadHandler.post(() -> {
                    if (transactionHistoryView != null) {
                        Log.d("THController", "Updating UI with " + finalFormattedList.size() + " transactions");
                        transactionHistoryView.updateData(finalFormattedList);
                    } else {
                        Log.e("THController", "transactionHistoryView is null. Cannot update UI");
                    }
                });
            }).start();
        }

        private boolean isAll(String s) {
            return s == null || s.trim().isEmpty() ||
                    s.equalsIgnoreCase("all") ||
                    s.equalsIgnoreCase("any");
        }







    }

    private static class TransactionManager {
        public static List<TransactionDetails> formatTransactionHistory(List<TransactionDetails> transactionList, Context context) {
            Log.d("THController", "formatTransactionHistory(): received " + transactionList.size() + " transactions");

            String currentUserEmail = UserSession.getUser().getUserEmail();
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



}

