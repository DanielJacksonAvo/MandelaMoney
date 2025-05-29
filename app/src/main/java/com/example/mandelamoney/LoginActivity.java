package com.example.mandelamoney;

import static android.icu.lang.UCharacter.toLowerCase;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;


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

    @SuppressLint("ClickableViewAccessibility")
    private void configurePasswordVisibility(ImageView imgPasswordIcon, EditText tbxUserPassword) {
        tbxUserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        imgPasswordIcon.setImageResource(R.drawable.img_password_icon); // Initially show lock

        AtomicReference<Boolean> isPasswordVisible = new AtomicReference<>(false);

        tbxUserPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    imgPasswordIcon.setImageResource(isPasswordVisible.get() ?
                            R.drawable.img_eye_closed : R.drawable.img_eye_open);
                } else {
                    imgPasswordIcon.setImageResource(R.drawable.img_password_icon);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        imgPasswordIcon.setOnClickListener(view -> {
            if (tbxUserPassword.getText().length() == 0) {
                return;
            }

            boolean temp = !isPasswordVisible.get();
            isPasswordVisible.set(temp);

            if (!isPasswordVisible.get()) {
                tbxUserPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgPasswordIcon.setImageResource(R.drawable.img_eye_closed);
            } else {
                tbxUserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgPasswordIcon.setImageResource(R.drawable.img_eye_open);
            }

            tbxUserPassword.setSelection(tbxUserPassword.getText().length());
        });
    }

}