package com.example.mandelamoney.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.model.User;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.MySQLConnector;
import com.example.mandelamoney.view.Iface.IDashboardView;
import com.example.mandelamoney.view.Iface.IHomeDashboardView;
import com.example.mandelamoney.view.Iface.ITransactionHistoryView;
import com.example.mandelamoney.view.activity.MakePaymentScanQrActivity;
import com.example.mandelamoney.view.activity.RequestPaymentEnterAmountActivity;
import com.example.mandelamoney.view.fragment.TransactionHistoryFragment;

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

    public void handleLoadUserToUITablet() {
        view.displayUserNameTablet(DashboardHomeController.getUserName());
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
            view.displayUserName(getUserName());
            startPolling();
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
                    UserSession.refreshTransactionHistory(context, () -> {
                        Log.d("DashboardPolling", "Transactions updated after balance change.");
                    });
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

//    private void pullSQLTransaction() {
//        ArrayList<Transaction> transactionList = new ArrayList<>();
//        // TODO: SQL logic to fill transactionList
//    }

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



    }


}
