package com.example.mandelamoney;

import static android.icu.lang.UCharacter.toLowerCase;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.SQLException;


public class LoginActivity extends AppCompatActivity {

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
        EditText tbxUserEmail = findViewById(R.id.tbx_email_login);
        EditText tbxUserPassword = findViewById(R.id.tbx_password_login);
        TextView txtError = findViewById(R.id.txt_error_login);
        configureLoginButton(btnLogin, tbxUserEmail, tbxUserPassword, txtError);

    }

    private void configureLoginButton(Button btnLogin, EditText tbxUserEmail, EditText tbxUserPassword, TextView txtError) {
        btnLogin.setOnClickListener((view) -> {
            String userEmail = toLowerCase(String.valueOf(tbxUserEmail.getText()));
            String userPassword = String.valueOf(tbxUserPassword.getText());
            try {
                checkEmailPassword(userEmail, userPassword, txtError);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        });
    }

    private void checkEmailPassword(String userEmail, String userPassword, TextView txtError) throws SQLException {
        User user = validateEmailPassword(userEmail, userPassword);
        if (checkForInvalidCredential(user)) {
            txtError.setVisibility(VISIBLE);
            return;
        } else {
            txtError.setVisibility(GONE);
            //continue here
        }

    }

    //method class class sql procedure "ValidateEmailPassword" and returns user object (valid) or null (invalid)
    private User validateEmailPassword(String userEmail, String userPassword) throws SQLException {
        return MySQLConnector.validateEmailPassword(userEmail, userPassword, this);
    }

    private boolean checkForInvalidCredential(User user) {
        return user == null;
    }

}