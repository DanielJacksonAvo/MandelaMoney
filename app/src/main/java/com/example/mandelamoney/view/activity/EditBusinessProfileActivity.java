package com.example.mandelamoney.view.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.EditProfileController;
import com.example.mandelamoney.model.Business;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IEditProfileView;

public class EditBusinessProfileActivity extends AppCompatActivity implements IEditProfileView {
    private EditText tbxEmail, tbxBusinessName, tbxPhone, tbxVAT;
    private TextView txtError, btnCancel;
    private Button btnSave;
    private ConstraintLayout loadingSpinner;
    private EditProfileController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_business_profile);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        connectToUi();
        controller = new EditProfileController(this, this);
        configureSaveButton();
        configureCancelButton();

    }

    private void connectToUi() {
        tbxEmail = findViewById(R.id.tbx_email_editbusinessprofile);
        tbxBusinessName = findViewById(R.id.tbx_name_editbusinessprofile);
        tbxPhone = findViewById(R.id.tbx_phone_editbusinessprofile);
        tbxVAT = findViewById(R.id.tbx_vat_editbusinessprofile);
        txtError = findViewById(R.id.txt_error_details_editbusinessprofile);
        btnCancel = findViewById(R.id.btn_cancel_editbusinessprofile);
        btnSave = findViewById(R.id.btn_save_editbusinessprofile);
        loadingSpinner = findViewById(R.id.editbusinessprofile_loading_spinner);
    }

    @Override
    public void loadUser() {
        tbxEmail.setText(UserSession.getUser().getUserEmail());
        tbxBusinessName.setText(((Business)(UserSession.getUser())).getBusinessName());
        tbxPhone.setText(((Business)(UserSession.getUser())).getBusinessPhoneNumber());
        tbxVAT.setText(((Business)(UserSession.getUser())).getBusinessVAT());
    }

    @Override
    public void showError(String error) {
        txtError.setText(error);
        txtError.setVisibility(TextView.VISIBLE);
    }

    @Override
    public void hideError() {
        txtError.setVisibility(TextView.GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showLoadingScreen() {
        loadingSpinner.setVisibility(ConstraintLayout.VISIBLE);
    }

    @Override
    public void hideLoadingScreen() {
        loadingSpinner.setVisibility(ConstraintLayout.GONE);
    }

    private void configureSaveButton() {
        btnSave.setOnClickListener((view) -> {
            String email = String.valueOf(tbxEmail.getText());
            String name = String.valueOf(tbxBusinessName.getText());
            String phone = String.valueOf(tbxPhone.getText());
            String vat = String.valueOf(tbxVAT.getText());
            controller.handleSaveButton(email, name, phone, vat);
        });
    }

    private void configureCancelButton() {
        btnCancel.setOnClickListener((view) -> {
            controller.handleCancelButton();
        });
    }
}