package com.example.mandelamoney;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateAccount_EnterBusinessDetailsActivity extends AppCompatActivity implements ICreateBusinessAccount{

    private CreateAccountController controller;
    TextView txtPasswordError;
    TextView txtDetailError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account_enter_business_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setController();
        connectToUI();
    }

    private void setController() {
        controller = (CreateAccountController) DataShare.receive();
        controller.setContextViewBusiness(this, this);
    }

    private void connectToUI() {
        TextView btnCancel = findViewById(R.id.btn_cancel_createbusinessaccount);
        Button btnCreateAccount = findViewById(R.id.btn_create_createbusinessaccount);
        EditText tbxPassword = findViewById(R.id.tbx_password_createbusinessaccount);
        EditText tbxPasswordReenter = findViewById(R.id.tbx_password_reenter_createbusinessaccount);
        ImageView imgPasswordIcon = findViewById(R.id.img_password_icon_createbusinessaccount);
        ImageView imgPasswordRenterIcon = findViewById(R.id.img_password_reenter_createbusinessaccount);
        //txtPasswordError = findViewById(R.id.txt_error_password_createbusinessaccount);
       // txtDetailError = findViewById(R.id.txt_error_details_createbusinessaccount);
        configureCancelButton(btnCancel);
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> {
            controller.handleCreateAccountCancel();
        });
    }

    @Override
    public void showPasswordError(String message) {

    }

    @Override
    public void hidePasswordError() {

    }

    @Override
    public void showDetailError(String message) {

    }

    @Override
    public void hideDetailError() {

    }
}