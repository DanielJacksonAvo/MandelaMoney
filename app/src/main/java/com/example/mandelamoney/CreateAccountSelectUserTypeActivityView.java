package com.example.mandelamoney;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateAccountSelectUserTypeActivityView extends AppCompatActivity implements ISelectUserTypeCreateAccountView {

    private CreateAccountController createAccountController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account_select_user_type);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        createAccountController = new CreateAccountController(this, this);
        connectToSelectUserTypeUI();

    }

    private void connectToSelectUserTypeUI() {
        Button btnStudentType = findViewById(R.id.btn_createaccount_student);
        Button btnBusinessType = findViewById(R.id.btn_createaccount_business);
        TextView btnCancel = findViewById(R.id.btn_cancel_selectaccounttype_createaccount);
        configureCancelButton(btnCancel);
        configureUserSelectButtons(btnStudentType, btnBusinessType);
    }

    private void connectToCreateStudentUI() {
        TextView btnCancel = findViewById(R.id.btn_cancel_createstudentaccount);
    }


    private void connectToCreateBusinessUI() {

    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> {
            createAccountController.handleSelectUserTypeCancel();
        });
    }

    private void configureUserSelectButtons(Button btnStudentType, Button btnBusinessType) {
        btnStudentType.setOnClickListener((view) -> {
            createAccountController.handleUserTypeSelection(1);
        });
        btnBusinessType.setOnClickListener((view) -> {
            createAccountController.handleUserTypeSelection(0);
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