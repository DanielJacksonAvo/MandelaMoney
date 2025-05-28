package com.example.mandelamoney;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import eightbitlab.com.blurview.BlurView;



public class LoginActivity extends AppCompatActivity {

    private EditText tbxUserEmail, tbxUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        connectToUI();
    }

    private void connectToUI() {
        Button btnLogin = findViewById(R.id.btn_login);
        tbxUserEmail = findViewById(R.id.tbx_email_login);
        tbxUserPassword = findViewById(R.id.tbx_password_login);



    }

    private void configureLoginButton(Button btnLogin) {
        btnLogin.setOnClickListener((view) -> {

        });
    }

    private void validateEmailPassword() {

    }

    private void createStudentObject() {

    }

    private void createBusinessObject() {

    }

}