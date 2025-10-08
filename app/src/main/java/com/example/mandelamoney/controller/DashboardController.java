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
import com.example.mandelamoney.model.Transaction;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.NetworkChecker;
import com.example.mandelamoney.util.TransactionManager;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.IDashboardView;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;
import com.example.mandelamoney.view.Iface.IProfileView;
import com.example.mandelamoney.view.Iface.ISettingsView;
import com.example.mandelamoney.view.Iface.ITransactionHistoryView;
import com.example.mandelamoney.view.activity.DepositFundsActivity;
import com.example.mandelamoney.view.activity.LoginActivity;
import com.example.mandelamoney.view.activity.MakePaymentScanQrActivity;
import com.example.mandelamoney.view.activity.RequestPaymentEnterAmountActivity;
import com.example.mandelamoney.view.activity.UnlockActivity;
import com.example.mandelamoney.view.fragment.SettingsDashboardFragment;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DashboardController {
    private final IDashboardView view;
    private final Context context;

    private int currentFragment = 0;

    public DashboardHomeController DashboardHomeController;
    public TransactionHistoryController TransactionHistoryController;
    public DashboardProfileController DashboardProfileController;
    public DashboardSettingsController DashboardSettingsController;



    public DashboardController(Context context, IDashboardView view) {
        this.context = context;
        this.view = view;
    }


    public void handleHome() {
        currentFragment = 0;
        view.displayHome();
        manageControllers();

    }

    public void handleLock() {
        currentFragment = 1;
        if (UserSession.getUser() != null) {
            UserSession.saveSession(context);
        }
        UserSession.clearSession();
        Intent intent = new Intent(context, UnlockActivity.class);
        context.startActivity(intent);
        view.finishActivity();
        manageControllers();
    }

    public void handleSettings() {
        currentFragment = 2;
        view.displaySettings();
        manageControllers();
    }

    public void handleProfile() {
        currentFragment = 3;
        DataShare.send(this);
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
        if (DashboardHomeController != null) {
            if ((currentFragment == 0) || (currentFragment == 3)) {
                DashboardHomeController.startPolling();
            } else {
                DashboardHomeController.stopPolling();
            }
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

    public void createDashboardProfileController(IProfileView view) {
        DashboardProfileController = new DashboardProfileController(view);
    }

    public void createDashboardSettingsController(ISettingsView view) {
        DashboardSettingsController = new DashboardSettingsController(view);
    }


    public class DashboardHomeController {
        private final IHomeDashboardView view;
        private ScheduledExecutorService scheduler;
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        private ScheduledFuture<?> pollingHandle;

        public DashboardHomeController(IHomeDashboardView view) {
            this.view = view;
        }

        public void handleLoadUserToUI() {
            try {
                User user = UserSession.getUser();
                if (user == null) {
                    Log.e("DashboardHomeController", "User is null! Cannot load user data.");
                    return;
                }
                view.displayBalance(user.getUserBalance());
                view.displayUserName(getUserName());
                displayTransactions(UserSession.getLastWeekTransactions());
            } catch (Exception e) {
                Log.e("DashboardHomeController", "Error in handleLoadUserToUI", e);
            }
        }

        public String getUserName() {
            if (UserSession.getUser() != null) {
                User user = UserSession.getUser();
                if (user instanceof Student) {
                    return ((Student) user).getStudentFullName();
                } else if (user instanceof Business) {
                    return ((Business) user).getBusinessName();
                }
            }
            return null;
        }

        public void handleBalanceRefresh() {
            if (UserSession.getUser() != null) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    float previousBalance = UserSession.getUser().getUserBalance();
                    float updatedBalance = UserSession.updateBalance(context);
                    if (updatedBalance != previousBalance) {
                        UserSession.getUser().setUserBalance(updatedBalance);
                        mainThreadHandler.post(() -> {
                            view.displayBalance(updatedBalance);
                            refreshAndDisplayTransactions();
                            if (TransactionHistoryController != null) {
                                ITransactionHistoryView view = TransactionHistoryController.getTransactionHistoryView();
                                String[] filters = view.getFilters();
                                TransactionHistoryController.loadTransactions(filters[0], filters[1], filters[2]);
                            }
                        });
                    }
                });
            }
        }

        public void handleMakePayment() {
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
            if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
                Log.d("DashboardHomeController", "Initializing new ScheduledExecutorService for polling.");
                scheduler = Executors.newSingleThreadScheduledExecutor();
            }

            if (pollingHandle != null && !pollingHandle.isDone() && !pollingHandle.isCancelled()) {
                Log.d("DashboardHomeController", "Polling already running. Skipping start.");
                return;
            }

            Log.d("DashboardHomeController", "Starting polling...");
            Runnable statusChecker = () -> {
                try {
                    handleBalanceRefresh();
                } catch (Exception e) {
                    Log.e("DashboardHomeController", "Error in polling task", e);
                }
            };

            pollingHandle = scheduler.scheduleWithFixedDelay(statusChecker, 0, 3, TimeUnit.SECONDS);
        }

        public void stopPolling() {
            Log.d("DashboardHomeController", "Stopping polling...");
            if (pollingHandle != null) {
                pollingHandle.cancel(true);
                pollingHandle = null;
            }
        }


        public void refreshAndDisplayTransactions() {
            new Thread(() -> {
                UserSession.updateTransactions(context);
                List<Transaction> txList = UserSession.getLastWeekTransactions();
                displayTransactions(txList);
            }).start();
        }

        private void displayTransactions(List<Transaction> transactions) {
                mainThreadHandler.post(() -> {
                    if (view != null) {
                        view.displayTransactions(transactions);
                    }
                });
        }

    }

    public class DashboardSettingsController {
        private final ISettingsView view;

        public DashboardSettingsController(ISettingsView view) {
            this.view = view;
        }

        public void loadUserToUI() {
            if (UserSession.getUser() instanceof Student) {
                view.displayUserName(((Student) UserSession.getUser()).getStudentFullName());
            } else {
                view.displayUserName(((Business) UserSession.getUser()).getBusinessName());
            }
        }

        private void displayNetworkStatus() {
            String status = NetworkChecker.checkConnection();
            view.displayConnectionQuality(status);
            if (status.equals("Disconnected")) {
                view.displayConnectionStatus(status);
            } else {
                view.displayConnectionStatus("Connected");
            }

        }
    }

    public class DashboardProfileController {
        IProfileView view;
        public DashboardProfileController(IProfileView view) {
            this.view = view;
        }

        public void loadUserToUi() {
            boolean isTabletLandscape = context.getResources().getBoolean(R.bool.is_tablet_landscape);
            if (isTabletLandscape) {
                if (UserSession.getUser() instanceof Student) {
                    view.setFirstNameLabel(context.getString(R.string.first_name_));
                    view.setFirstName(((Student) UserSession.getUser()).getStudentFirstName());
                    view.setLastNameLabel(context.getString(R.string.last_name_));
                    view.setLastName(((Student) UserSession.getUser()).getStudentLastName());
                    view.setStudentNumberLabel(context.getString(R.string.student_number_));
                    view.setStudentNumber(((Student) UserSession.getUser()).getStudentNumber());
                } else if (UserSession.getUser() instanceof Business) {
                    view.setFirstNameLabel(context.getString(R.string.business_name_));
                    view.setFirstName((((Business) UserSession.getUser()).getBusinessName()));
                    view.setLastNameLabel(context.getString(R.string.vat_number_));
                    view.setLastName(((Business) UserSession.getUser()).getBusinessVAT());
                    view.setStudentNumberLabel(context.getString(R.string.phone_number_));
                    view.setStudentNumber(((Business) UserSession.getUser()).getBusinessPhoneNumber());
                }
                view.setEmail(UserSession.getUser().getUserEmail());
                view.setBalance((float) UserSession.getUser().getUserBalance());

            } else {
                if (UserSession.getUser() instanceof Student) {
                    view.setWelcomeName(((Student) UserSession.getUser()).getStudentFullName());
                    view.setFirstNameLabel(context.getString(R.string.first_name_));
                    view.setFirstName(((Student) UserSession.getUser()).getStudentFirstName());
                    view.setLastNameLabel(context.getString(R.string.last_name_));
                    view.setLastName(((Student) UserSession.getUser()).getStudentLastName());
                    view.setStudentNumberLabel(context.getString(R.string.student_number_));
                    view.setStudentNumber(((Student) UserSession.getUser()).getStudentNumber());
                } else if (UserSession.getUser() instanceof Business) {
                    view.setWelcomeName((((Business) UserSession.getUser()).getBusinessName()));
                    view.setFirstNameLabel(context.getString(R.string.business_name_));
                    view.setFirstName((((Business) UserSession.getUser()).getBusinessName()));
                    view.setLastNameLabel(context.getString(R.string.vat_number_));
                    view.setLastName(((Business) UserSession.getUser()).getBusinessVAT());
                    view.setStudentNumberLabel(context.getString(R.string.phone_number_));
                    view.setStudentNumber(((Business) UserSession.getUser()).getBusinessPhoneNumber());
                }
                view.setEmail(UserSession.getUser().getUserEmail());
                view.setBalance((float) UserSession.getUser().getUserBalance());
            }


        }

        public void handleEditButton() {
            /// edit profile activity
        }

        public void handleDepositButton() {
            User u = UserSession.getUser();
            if (u == null) {
                context.startActivity(new Intent(context, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                return;
            }
            com.example.mandelamoney.util.DataShare.send(u);   // <-- send user
            Intent intent = new Intent(context, com.example.mandelamoney.view.activity.DepositFundsActivity.class);
            context.startActivity(intent);
        }


        public void handleChangePasswordButton() {
            ///  change password activity
        }

        public void handleLogoutButton() {
            UserSession.deleteSession(context);
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            //DashboardController.this.view.finishActivity();
        }

    }

    public class TransactionHistoryController {

        private final ITransactionHistoryView transactionHistoryView;
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        public TransactionHistoryController(ITransactionHistoryView transactionHistoryView) {
            this.transactionHistoryView = transactionHistoryView;
        }

        public ITransactionHistoryView getTransactionHistoryView() {
            return transactionHistoryView;
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
            if (searchQuery == null && period == null && type == null) {
                mainThreadHandler.post(() -> {
                    if (transactionHistoryView != null) {
                        Log.d("THController", "Updating UI with cache");
                        transactionHistoryView.updateData(UserSession.getTransactions());
                    } else {
                        Log.e("THController", "transactionHistoryView is null. Cannot update UI");
                    }
                });
            } else {
                new Thread(() -> {
                    User currentUser = UserSession.getUser();
                    if (currentUser == null) {
                        Log.e("THController", "User is null, cannot load transactions.");
                        return;
                    }
                    String userEmail = currentUser.getUserEmail();
                    Log.d("THController", "loadTransactions() called. Query=" + searchQuery + " Period=" + period + " Type=" + type + " User=" + userEmail);

                    boolean periodAll = isAll(period);
                    boolean typeAll   = isAll(type);

                    List<Transaction> rawList;
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

                    Log.d("THController", "Formatting transactions...");
                    List<Transaction> formattedList = TransactionManager.formatTransactionHistory(rawList, context);
                    Log.d("THController", "Formatting complete. Total: " + formattedList.size());

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

                    List<Transaction> finalFormattedList = formattedList;
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

        }

        private boolean isAll(String s) {
            return s == null || s.trim().isEmpty() ||
                    s.equalsIgnoreCase("all") ||
                    s.equalsIgnoreCase("any");
        }
    }
}