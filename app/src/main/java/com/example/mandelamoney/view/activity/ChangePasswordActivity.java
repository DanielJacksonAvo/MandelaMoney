package com.example.mandelamoney.view.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
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
import com.example.mandelamoney.controller.ChangePasswordController;
import com.example.mandelamoney.util.ErrorBorder;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IChangePasswordView;

import java.util.concurrent.atomic.AtomicReference;

public class ChangePasswordActivity extends AppCompatActivity implements IChangePasswordView {
    private ChangePasswordController changePasswordController;
    TextView txtChangePasswordErrorCurrent;
    TextView txtChangePasswordErrorNew;
    private ConstraintLayout loadingSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (UserSession.getUser() == null) {
            Log.w("ChangePasswordActivity", "No session; routing to LoginActivity");
            startActivity(new Intent(this, com.example.mandelamoney.view.activity.LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        this.changePasswordController = new ChangePasswordController(this, this);
        connectToUI();
    }
    private void connectToUI(){
        Button btnChangePassword = findViewById(R.id.btn_change_password);
        ImageView imgPasswordIcon = findViewById(R.id.img_password_changepassword);
        ImageView imgNewPasswordIcon = findViewById(R.id.img_new_password_changepassword);
        ImageView imgConfirmNewPasswordIcon = findViewById(R.id.img_confirm_password_changepassword);
        TextView btnCancelChangePassword = findViewById(R.id.btn_cancel_change_password);
        EditText tbxPassword = findViewById(R.id.tbx_password_changepassword);
        EditText tbxNewPassword = findViewById(R.id.tbx_new_password_changepassword);
        EditText tbxConfirmNewPassword = findViewById(R.id.tbx_confirm_password_changepassword);
        txtChangePasswordErrorCurrent = findViewById(R.id.txt_error_current_password_change_password);
        txtChangePasswordErrorNew = findViewById(R.id.txt_error_new_password_change_password);
        configureChangePasswordButton(btnChangePassword,tbxPassword,tbxNewPassword,tbxConfirmNewPassword);
        configurePasswordVisibility(imgPasswordIcon,tbxPassword);
        configurePasswordVisibility(imgNewPasswordIcon,tbxNewPassword);
        configurePasswordVisibility(imgConfirmNewPasswordIcon,tbxConfirmNewPassword);
        configureCancel(btnCancelChangePassword);
        loadingSpinner = findViewById(R.id.change_password_loading_spinner);
    }


    @Override
    public void showCurrentPasswordError(String s) {
        EditText tbxCurrentPassword = findViewById(R.id.tbx_password_changepassword);
        ErrorBorder.applyMandelaYellowBorder(tbxCurrentPassword);
        txtChangePasswordErrorCurrent.setVisibility(VISIBLE);
        txtChangePasswordErrorCurrent.setText(s);
    }

    @Override
    public void hideCurrentPasswordError() {
        EditText tbxCurrentPassword = findViewById(R.id.tbx_password_changepassword);
        ErrorBorder.removeStroke(tbxCurrentPassword);
        txtChangePasswordErrorCurrent.setVisibility(GONE);
    }

    @Override
    public void showNewPasswordError(String s) {
        EditText tbxNewPassword = findViewById(R.id.tbx_new_password_changepassword);
        EditText tbxConfirmPassword = findViewById(R.id.tbx_confirm_password_changepassword);
        ErrorBorder.applyMandelaYellowBorder(tbxNewPassword);
        ErrorBorder.applyMandelaYellowBorder(tbxConfirmPassword);
        txtChangePasswordErrorNew.setVisibility(VISIBLE);
        txtChangePasswordErrorNew.setText(s);
    }

    @Override
    public void hideNewPasswordError() {
        EditText tbxNewPassword = findViewById(R.id.tbx_new_password_changepassword);
        EditText tbxConfirmPassword = findViewById(R.id.tbx_confirm_password_changepassword);
        ErrorBorder.removeStroke(tbxNewPassword);
        ErrorBorder.removeStroke(tbxConfirmPassword);
        txtChangePasswordErrorNew.setVisibility(GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void hideLoadingSpinner() {
        runOnUiThread(() -> {
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void showLoadingSpinner() {
        runOnUiThread(() -> {
            if (loadingSpinner != null) {
                loadingSpinner.setVisibility(View.VISIBLE);
            }
        });
    }

    private void configureCancel(TextView btnCancel) {
        btnCancel.setOnClickListener((view)-> changePasswordController.handleCancel());
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

    private void configureChangePasswordButton(Button btnChangePassword,EditText tbxPassword, EditText tbxNewPassword,EditText tbxConfirmNewPassword){
        btnChangePassword.setOnClickListener((view)->{
            String password = String.valueOf(tbxPassword.getText()).trim();
            String newPassword = String.valueOf(tbxNewPassword.getText()).trim();
            String confirmNewPassword = String.valueOf(tbxConfirmNewPassword.getText()).trim();
            String userEmail = UserSession.getUser().getUserEmail();
            changePasswordController.handleChangePassword(userEmail,password,newPassword,confirmNewPassword);

        });
    }

}
