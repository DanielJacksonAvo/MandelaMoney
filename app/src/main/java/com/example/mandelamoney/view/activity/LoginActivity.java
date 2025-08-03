package com.example.mandelamoney.view.activity;

import static android.icu.lang.UCharacter.toLowerCase;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.LoginController;
import com.example.mandelamoney.view.Iface.ILoginView;
import java.util.concurrent.atomic.AtomicReference;

public class LoginActivity extends AppCompatActivity implements ILoginView {
    private LoginController loginController;
    private TextView txtError;

    private Button btnLogin;
    private EditText tbxUserEmail;
    private EditText tbxUserPassword;
    private ImageView imgPasswordIcon;
    private TextView btnForgotPassword;
    private TextView btnSignup;
    private ConstraintLayout loadingSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        loginController = new LoginController(this, this);
        View tabletSpecificViewCheck = findViewById(R.id.blurView_login1);

        if (tabletSpecificViewCheck != null) {
            Log.d("LoginActivity", "Tablet layout (layout-sw600dp) loaded.");
            setupTabletUI();
        } else {
            Log.d("LoginActivity", "Phone layout (default layout) loaded.");
            setupPhoneUI();
        }
        hideLoadingSpinner();
    }

    private void setupPhoneUI() {
        btnLogin = findViewById(R.id.btn_login);
        tbxUserEmail = findViewById(R.id.tbx_email_login);
        tbxUserPassword = findViewById(R.id.tbx_password_login);
        txtError = findViewById(R.id.txt_error_login);
        imgPasswordIcon = findViewById(R.id.img_password_login);
        btnForgotPassword = findViewById(R.id.btn_forgotPassword_login);
        btnSignup = findViewById(R.id.btn_signup_login);
        loadingSpinner = findViewById(R.id.login_loading_spinner);

        if (btnLogin != null && tbxUserEmail != null && tbxUserPassword != null) {
            configureLoginButton(btnLogin, tbxUserEmail, tbxUserPassword);
        }
        if (imgPasswordIcon != null && tbxUserPassword != null) {
            configurePasswordVisibility(imgPasswordIcon, tbxUserPassword);
        }
        if (btnForgotPassword != null) {
            configureForgotPasswordButton(btnForgotPassword);
        }
        if (btnSignup != null) {
            configureSignupButton(btnSignup);
        }
    }

    private void setupTabletUI() {
        btnLogin = findViewById(R.id.btn_login3);
        tbxUserEmail = findViewById(R.id.tbx_email_login);
        tbxUserPassword = findViewById(R.id.tbx_password_login);
        txtError = findViewById(R.id.txt_error_login);
        imgPasswordIcon = findViewById(R.id.img_password_login);
        btnForgotPassword = findViewById(R.id.btn_forgotPassword_login);
        btnSignup = findViewById(R.id.btn_signup_login2);
        loadingSpinner = findViewById(R.id.login_loading_spinner);

        if (btnLogin != null && tbxUserEmail != null && tbxUserPassword != null) {
            configureLoginButton(btnLogin, tbxUserEmail, tbxUserPassword);
        }
        if (imgPasswordIcon != null && tbxUserPassword != null) {
            configurePasswordVisibility(imgPasswordIcon, tbxUserPassword);
        }
        if (btnForgotPassword != null) {
            configureForgotPasswordButton(btnForgotPassword);
        }
        if (btnSignup != null) {
            configureSignupButton(btnSignup);
        }
    }

    private void configureLoginButton(Button btnLogin, EditText tbxUserEmail, EditText tbxUserPassword) {
        if (btnLogin != null) {
            btnLogin.setOnClickListener((view) -> {
                String userEmail = toLowerCase(String.valueOf(tbxUserEmail.getText()));
                String userPassword = String.valueOf(tbxUserPassword.getText());
                loginController.handleLogin(userEmail, userPassword);
            });
        } else {
            Log.e("LoginActivity", "Login button is null in configureLoginButton.");
        }
    }

    private void configureForgotPasswordButton(TextView btnForgotPassword){
        if (btnForgotPassword != null) {
            btnForgotPassword.setOnClickListener((view)-> loginController.handleForgotPassword());
        } else {
            Log.e("LoginActivity", "Forgot Password button is null in configureForgotPasswordButton.");
        }
    }

    private void configureSignupButton(TextView btnSignup) {
        if (btnSignup != null) {
            btnSignup.setOnClickListener((view) -> loginController.handleSignUp());
        } else {
            Log.e("LoginActivity", "Sign Up button is null in configureSignupButton.");
        }
    }

    @Override
    public void showErrorMessage() {
        if (txtError != null) {
            txtError.setVisibility(VISIBLE);
        }
    }

    @Override
    public void hideErrorMessage() {
        if (txtError != null) {
            txtError.setVisibility(GONE);
        }
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void hideLoadingSpinner() {
        loadingSpinner.setVisibility(GONE);
    }

    @Override
    public void showLoadingSpinner() {
        loadingSpinner.setVisibility(VISIBLE);
    }

    private void configurePasswordVisibility(ImageView imgPasswordIcon, EditText tbxUserPassword) {
        if (tbxUserPassword == null || imgPasswordIcon == null) {
            Log.e("LoginActivity", "Password EditText or Icon is null in configurePasswordVisibility.");
            return;
        }

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
