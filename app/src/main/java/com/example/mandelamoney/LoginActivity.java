package com.example.mandelamoney;

import static android.icu.lang.UCharacter.toLowerCase;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;


public class LoginActivity extends AppCompatActivity implements ILoginView {
    private LoginController loginController;
    private TextView txtError;
    private EditText tbxUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Attempt to restore session before calling super
//        UserSessionLoader.tryRestoreSession(this);
//        if (UserSession.getUser() != null) {
//            Intent intent = new Intent(this, DashboardActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }

        // Proceed normally
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginController = new LoginController(this, this);
        connectToUI();
    }


    private void connectToUI() {
        Button btnLogin = findViewById(R.id.btn_login);
        EditText tbxUserEmail = findViewById(R.id.tbx_studentnumber_createstudentaccount);
        tbxUserPassword = findViewById(R.id.tbx_password_reenter_createstudentaccount);
        txtError = findViewById(R.id.txt_error_login);
        TextView btnSignup = findViewById(R.id.btn_signup_login);
        ImageView imgPasswordIcon = findViewById(R.id.img_password_reenter_createstudentaccount);
        configureLoginButton(btnLogin, tbxUserEmail, tbxUserPassword);
        configurePasswordVisibility(imgPasswordIcon, tbxUserPassword);
        configureSignupButton(btnSignup);
    }

    private void configureLoginButton(Button btnLogin, EditText tbxUserEmail, EditText tbxUserPassword) {
        btnLogin.setOnClickListener((view) -> {
            String userEmail = toLowerCase(String.valueOf(tbxUserEmail.getText()));
            String userPassword = String.valueOf(tbxUserPassword.getText());
            try {
                loginController.handleLogin(userEmail, userPassword);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        });
    }

    private void configureSignupButton(TextView btnSignup) {
        btnSignup.setOnClickListener((view) -> {
            loginController.handleSignUp();
        });
    }

    @Override
    public void showErrorMessage() {
        txtError.setVisibility(VISIBLE);
    }

    @Override
    public void hideErrorMessage() {
        txtError.setVisibility(GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }


    private void configurePasswordVisibility(ImageView imgPasswordIcon, EditText tbxUserPassword) {
        tbxUserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tbxUserPassword.setTransformationMethod(new PasswordTransformationMethod());
        imgPasswordIcon.setImageResource(R.drawable.img_password_icon);

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

            if (isPasswordVisible.get()) {
                tbxUserPassword.setTransformationMethod(null);
                imgPasswordIcon.setImageResource(R.drawable.img_eye_closed);
            } else {
                tbxUserPassword.setTransformationMethod(new PasswordTransformationMethod());
                imgPasswordIcon.setImageResource(R.drawable.img_eye_open);
            }

            tbxUserPassword.setSelection(tbxUserPassword.getText().length());
        });
    }

}