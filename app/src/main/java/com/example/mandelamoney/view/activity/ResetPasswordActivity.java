package com.example.mandelamoney.view.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
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
import com.example.mandelamoney.controller.ForgotPasswordController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.ErrorBorder;
import com.example.mandelamoney.view.Iface.IResetPasswordView;

import java.util.concurrent.atomic.AtomicReference;

public class ResetPasswordActivity extends AppCompatActivity implements IResetPasswordView {
    private ForgotPasswordController controller;
    private TextView txtError;
    private ConstraintLayout loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);
        String userEmail = getIntent().getStringExtra("userEmail");
        String recoveryCode = getIntent().getStringExtra("recoveryCode");
        if (userEmail == null || recoveryCode == null) {
            finish();
            return;
        }
        controller = new ForgotPasswordController(this, /* forgotPasswordView */ null);
        controller.setContext(this);
        controller.setResetPasswordView(this);
        controller.setUserEmail(userEmail);
        controller.setUserRecoveryCode(recoveryCode);
        connectToUI();
        hideErrorMessage_PasswordsDoNotMatch();
        hideErrorMessage_Minimum8Characters();
    }
    
    private void getController() {
        controller = (ForgotPasswordController) DataShare.receive();
        if (controller == null) {
            controller = new ForgotPasswordController(this, /* forgotPasswordView = */ null);
        }
        controller.setContext(this);
        controller.setResetPasswordView(this);
        controller = (ForgotPasswordController) DataShare.receive();

    }

    private void connectToUI() {
        Button btnResetPassword = findViewById(R.id.btn_reset_password);
        EditText tbxNewPassword = findViewById(R.id.tbx_password_reset_password);
        EditText tbxConfirmNewPassword = findViewById(R.id.tbx_confirm_password_reset_password);
        txtError = findViewById(R.id.txt_error_reset_password);
        TextView btnCancel = findViewById(R.id.btn_cancel_reset_password);
        ImageView imgPasswordIcon_password = findViewById(R.id.img_password_icon_reset_password);
        ImageView imgPasswordIcon_confirmPassword = findViewById(R.id.img_confirm_password_icon_reset_password);
        loadingSpinner = findViewById(R.id.reset_password_loading_spinner);
        configureResetPasswordButton(btnResetPassword, tbxNewPassword, tbxConfirmNewPassword,txtError);
        configurePasswordVisibility(imgPasswordIcon_password, tbxNewPassword);
        configurePasswordVisibility(imgPasswordIcon_confirmPassword, tbxConfirmNewPassword);
        configureCancel(btnCancel);
    }

    private void configureCancel(TextView btnCancel) {
        btnCancel.setOnClickListener((view)-> controller.handleCancel());
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

    private void configureResetPasswordButton(Button btnResetPassword, EditText tbxNewPassword, EditText tbxConfirmNewPassword, TextView txtError) {
        btnResetPassword.setOnClickListener((view)->{
            String newPassword = String.valueOf(tbxNewPassword.getText());
            String confirmPassword = String.valueOf(tbxConfirmNewPassword.getText());
            controller.handleResetPassword(newPassword,confirmPassword);
        });
    }

    @Override
    public void showErrorMessage_PasswordsDoNotMatch(String string) {
        EditText tbxNewPassword = findViewById(R.id.tbx_password_reset_password);
        EditText tbxConfirmPassword = findViewById(R.id.tbx_confirm_password_reset_password);
        ErrorBorder.applyMandelaYellowBorder(tbxConfirmPassword);
        ErrorBorder.applyMandelaYellowBorder(tbxNewPassword);
        txtError.setText(string);
        txtError.setVisibility(VISIBLE);
    }

    @Override
    public void hideErrorMessage_PasswordsDoNotMatch() {
        EditText tbxNewPassword = findViewById(R.id.tbx_password_reset_password);
        EditText tbxConfirmPassword = findViewById(R.id.tbx_confirm_password_reset_password);
        ErrorBorder.removeStroke(tbxConfirmPassword);
        ErrorBorder.removeStroke(tbxNewPassword);
        txtError.setVisibility(GONE);
    }

    @Override
    public void showErrorMessage_Minimum8Characters(String string) {
        EditText tbxNewPassword = findViewById(R.id.tbx_password_reset_password);
        EditText tbxConfirmPassword = findViewById(R.id.tbx_confirm_password_reset_password);
        ErrorBorder.applyMandelaYellowBorder(tbxConfirmPassword);
        ErrorBorder.applyMandelaYellowBorder(tbxNewPassword);
        txtError.setText(string);
        txtError.setVisibility(VISIBLE);
    }

    @Override
    public void hideErrorMessage_Minimum8Characters() {
        EditText tbxNewPassword = findViewById(R.id.tbx_password_reset_password);
        EditText tbxConfirmPassword = findViewById(R.id.tbx_confirm_password_reset_password);
        ErrorBorder.removeStroke(tbxConfirmPassword);
        ErrorBorder.removeStroke(tbxNewPassword);
        txtError.setVisibility(GONE);
    }
    @Override
    public void showLoadingSpinner(){
        runOnUiThread(() -> {
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.VISIBLE);
            }
        });
    }
    @Override
    public void hideLoadingSpinner(){
        runOnUiThread(() -> {
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
