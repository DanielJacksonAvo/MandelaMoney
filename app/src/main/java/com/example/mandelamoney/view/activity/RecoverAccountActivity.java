package com.example.mandelamoney.view.activity;


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
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.ForgotPasswordController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.IRecoverAccountView;

import java.sql.SQLException;

public class RecoverAccountActivity extends AppCompatActivity implements IRecoverAccountView {
    private ForgotPasswordController controller;
    private TextView txtError;
    EditText tbxRecoveryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recover_account);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);
        String userEmail = getIntent().getStringExtra("userEmail");
        if(userEmail == null){
            finish();
            return;
        }
        getController(userEmail);
        connectToUI();
    }

    private void getController(String userEmail) {
        controller = (ForgotPasswordController) DataShare.receive();
        controller.setContext(this);
        controller.setRecoverAccountView(this);
        controller.setUserEmail(userEmail);
    }

    private void connectToUI(){
        Button btnVerify = findViewById(R.id.btn_verify);
        TextView btnCancel = findViewById(R.id.btn_cancel_recover_account);
        tbxRecoveryCode = findViewById(R.id.tbx_code);
        txtError = findViewById(R.id.txt_error_recover_account);
        configureVerifyButton(btnVerify, tbxRecoveryCode);
        configureCancelButton(btnCancel);

    }
    private void configureVerifyButton(Button btnVerify, EditText tbxRecoveryCode){
        btnVerify.setOnClickListener((view)->{
            String recoveryCode = toLowerCase(String.valueOf(tbxRecoveryCode.getText()));
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
        txtError.setVisibility(VISIBLE);
    }

    @Override
    public void hideErrorMessage_InvalidCode() {
        txtError.setVisibility(GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
