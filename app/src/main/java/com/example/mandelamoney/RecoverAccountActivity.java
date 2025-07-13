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

public class RecoverAccountActivity extends AppCompatActivity implements IRecoverAccountView{
    private RecoverAccountController recoverAccountController;
    private TextView txtError;
    EditText tbxRecoveryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recover_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),(v,insets)->{
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String userEmail = getIntent().getStringExtra("userEmail");
        if(userEmail == null){
            finish();
            return;
        }
        recoverAccountController = new RecoverAccountController(this,this);
        recoverAccountController.setUserEmail(userEmail);
        connectToUI();
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
                recoverAccountController.handleVerify(recoveryCode);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void configureCancelButton(TextView btnCancel){
        btnCancel.setOnClickListener((view)->{
            recoverAccountController.handleCancel();
        });
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
