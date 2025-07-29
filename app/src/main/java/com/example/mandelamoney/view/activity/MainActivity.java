package com.example.mandelamoney.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.util.MySQLConnector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int RETRY_DELAY_MS = 3000;
    private static final int MAX_RETRIES = 5;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        connectToDatabaseAndProceed();
    }

    private void connectToDatabaseAndProceed() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean connected = false;
            try {
                connected = MySQLConnector.AppStartUpConnection(MainActivity.this);
            } catch (Exception e) {
                connected = false;
            }

            final boolean finalConnected = connected;

            handler.post(() -> {
                if (finalConnected) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        handler.postDelayed(this::connectToDatabaseAndProceed, RETRY_DELAY_MS);
                    } else {
                        finishAffinity();
                    }
                }
            });
        });
    }
}