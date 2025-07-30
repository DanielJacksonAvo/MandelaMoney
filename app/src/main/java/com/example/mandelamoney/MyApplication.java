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

    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "Application is in foreground (onStart)");
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                Log.d(TAG, "Application is in background (onStop)");
                lockBackground();
            }
        });
    }

    private void lockBackground() {
        Log.d(TAG, "Executing lockBackground for entire app background!");

        android.content.Context context = this.getApplicationContext();

        if (UserSession.getUser() != null) {
            UserSession.saveSession(context);
        }

        UserSession.clearSession();

        Intent intent = new Intent(context, UnlockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}