package com.example.mandelamoney;

import static android.icu.lang.UCharacter.toLowerCase;
import static android.widget.Toast.LENGTH_LONG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.SQLException;


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
        configureLoginButton(btnLogin);
        tbxUserEmail = findViewById(R.id.tbx_email_login);
        tbxUserPassword = findViewById(R.id.tbx_password_login);
        ImageView imgPasswordIcon = findViewById(R.id.img_password_icon);
        configurePasswordVisibility(imgPasswordIcon, tbxUserPassword);

    }

    private void configureLoginButton(Button btnLogin) {
        btnLogin.setOnClickListener((view) -> {
            String userEmail = toLowerCase(String.valueOf(tbxUserEmail.getText()));
            String userPassword = String.valueOf(tbxUserPassword.getText());
            try {
                checkEmailPassword(userEmail, userPassword);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        });
    }

    private void checkEmailPassword(String userEmail, String userPassword) throws SQLException {
        User user = validateEmailPassword(userEmail, userPassword);
        if (checkForInvalidCredential(user)) {
            return;
        } else {
            //continue here
        }

    }

    //method class class sql procedure "ValidateEmailPassword" and returns user (valid) or null (invalid)
    private User validateEmailPassword(String userEmail, String userPassword) throws SQLException {
        return MySQLConnector.validateEmailPassword(userEmail, userPassword, this);
    }

    private boolean checkForInvalidCredential(User user) {
        return user == null;
    }

    private void configurePasswordVisibility(ImageView imgPasswordIcon, EditText tbxUserPassword) {
        imgPasswordIcon.setOnClickListener(view -> {

        });
    }

}