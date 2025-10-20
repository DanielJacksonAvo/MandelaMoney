package com.example.mandelamoney.view.activity;


import static android.icu.lang.UCharacter.toLowerCase;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.ForgotPasswordController;
import com.example.mandelamoney.util.ErrorBorder;
import com.example.mandelamoney.view.Iface.IRecoverAccountView;

import java.sql.SQLException;

public class RecoverAccountActivity extends AppCompatActivity implements IRecoverAccountView {
    private ForgotPasswordController controller;
    private TextView txtError;
    EditText tbxRecoveryCode;

    private ConstraintLayout loadingSpinner;
    private boolean emailExists = false;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recover_account);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);
        emailExists = getIntent().getBooleanExtra("emailExists", false);
        String userEmail = getIntent().getStringExtra("userEmail");
        if(userEmail == null){
            finish();
            return;
        }
        getController(userEmail);
        connectToUI();
        hideErrorMessage_InvalidCode();
    }

    private void getController(String userEmail) {
        controller = new ForgotPasswordController(this, /* forgotPasswordView */ null);
        controller.setContext(this);
        controller.setRecoverAccountView(this);
        controller.setUserEmail(userEmail);
    }

    private void connectToUI(){
        Button btnVerify = findViewById(R.id.btn_verify);
        TextView btnCancel = findViewById(R.id.btn_cancel_recover_account);
        tbxRecoveryCode = findViewById(R.id.tbx_code);
        txtError = findViewById(R.id.txt_error_recover_account);

        loadingSpinner = findViewById(R.id.recover_account_loading_spinner);
        configureVerifyButton(btnVerify, tbxRecoveryCode);
        configureCancelButton(btnCancel);
    }
    private void configureVerifyButton(Button btnVerify, EditText tbxRecoveryCode){
        btnVerify.setOnClickListener((view)->{

            if(!emailExists){
                showErrorMessage_InvalidCode();
                return;
            }
            String recoveryCode = toLowerCase(String.valueOf(tbxRecoveryCode.getText()));
            hideErrorMessage_InvalidCode();
            try{
                controller.handleVerify(recoveryCode);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void configureCancelButton(TextView btnCancel){
        btnCancel.setOnClickListener((view)-> controller.handleCancel());
    }
    @Override
    public void showErrorMessage_InvalidCode() {
        ErrorBorder.applyMandelaYellowBorder(tbxRecoveryCode);
        txtError.setVisibility(VISIBLE);
    }

    @Override
    public void hideErrorMessage_InvalidCode() {
        ErrorBorder.removeStroke(tbxRecoveryCode);
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  // Bring ActivityB to the front
        startActivity(intent);
        finishActivity();
        super.onBackPressed();
    }

}
