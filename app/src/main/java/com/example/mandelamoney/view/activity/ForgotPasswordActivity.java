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
import com.example.mandelamoney.view.Iface.IForgotPasswordView;

import java.sql.SQLException;

public class ForgotPasswordActivity extends AppCompatActivity implements IForgotPasswordView {

    private ForgotPasswordController forgotPasswordController;
    private TextView txtError;
    EditText tbxUserEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        forgotPasswordController = new ForgotPasswordController(this, this);
        connectToUI();
    }
    private void connectToUI(){
        Button btnRecover = findViewById(R.id.btn_recover);
        tbxUserEmail = findViewById(R.id.tbx_email_login_forgot_password);
        txtError = findViewById(R.id.txt_error_forgot_password);
        TextView btnCancel = findViewById(R.id.btn_cancel_forgot_password);
        configureRecoverButton(btnRecover, tbxUserEmail);
        configureCancelButton(btnCancel);
    }
    private void configureRecoverButton(Button btnRecover, EditText tbxUserEmail){
        btnRecover.setOnClickListener((view)->{
            String userEmail = toLowerCase(String.valueOf(tbxUserEmail.getText()));
            try{
                forgotPasswordController.handleForgotPassword(userEmail);
            }catch(SQLException e){
                throw new RuntimeException(e);
            }
        });
    }
    public void configureCancelButton(TextView btnCancel){
        btnCancel.setOnClickListener((view)-> forgotPasswordController.handleCancel());
    }
    @Override
    public void showErrorMessage_InvalidEmail() {
        txtError.setVisibility(VISIBLE);
    }

    @Override
    public void hideErrorMessage_InvalidEmail() {
        txtError.setVisibility(GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
