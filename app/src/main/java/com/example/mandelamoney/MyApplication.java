package com.example.mandelamoney;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.activity.UnlockActivity;

public class MyApplication extends Application {

    private static final String TAG = "AppLifecycleObserver";
    private long lastBackgroundTime = 0L;
    private boolean userWasLoggedInWhenBackgrounded = false;

    private static final long SESSION_TIMEOUT_MS = 10000;

    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "Application is in foreground (onStart)");
                handleForegroundUnlockCheck();
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "Application is in background (onStop)");
                lastBackgroundTime = System.currentTimeMillis();
                userWasLoggedInWhenBackgrounded = (UserSession.getUser() != null);
            }
        });
    }

    private void handleForegroundUnlockCheck() {
        boolean shouldUnlock = userWasLoggedInWhenBackgrounded &&
                (System.currentTimeMillis() - lastBackgroundTime > SESSION_TIMEOUT_MS) &&
                (UserSession.getUser() != null);

        if (shouldUnlock) {
            Log.d(TAG, "Session expired or cleared after timeout. Launching UnlockActivity.");

            UserSession.clearSession();

            android.content.Context context = this.getApplicationContext();
            Intent intent = new Intent(context, UnlockActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);

            userWasLoggedInWhenBackgrounded = false;

        } else if (UserSession.getUser() == null && !userWasLoggedInWhenBackgrounded) {
            Log.d(TAG, "No user logged in or no timeout. Not launching unlock screen.");
        } else {
            Log.d(TAG, "Session still active or timeout not reached. Continuing without unlock.");
        }
    }
}