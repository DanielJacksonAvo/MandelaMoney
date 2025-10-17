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
import com.example.mandelamoney.util.ErrorBorder;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IEditProfileView;

public class EditBusinessProfileActivity extends AppCompatActivity implements IEditProfileView {
    private EditText tbxBusinessName, tbxPhone, tbxVAT;
    private TextView txtBusinessNameError, txtPhoneError, txtVATError, btnCancel;
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
        tbxBusinessName = findViewById(R.id.tbx_name_editbusinessprofile);
        tbxPhone = findViewById(R.id.tbx_phone_editbusinessprofile);
        tbxVAT = findViewById(R.id.tbx_vat_editbusinessprofile);
        btnCancel = findViewById(R.id.btn_cancel_editbusinessprofile);
        btnSave = findViewById(R.id.btn_save_editbusinessprofile);
        loadingSpinner = findViewById(R.id.editbusinessprofile_loading_spinner);
        txtBusinessNameError = findViewById(R.id.txt_businessname_error_editbusinessprofile);
        txtPhoneError = findViewById(R.id.txt_phone_error_editbusinessprofile);
        txtVATError = findViewById(R.id.txt_vat_error_editbusinessprofile);
    }

    @Override
    public void loadUser() {
        tbxBusinessName.setText(((Business)(UserSession.getUser())).getBusinessName());
        tbxPhone.setText(((Business)(UserSession.getUser())).getBusinessPhoneNumber());
        tbxVAT.setText(((Business)(UserSession.getUser())).getBusinessVAT());
    }

    @Override
    public void showError(String error) {
        txtVATError.setText(error);
        txtVATError.setVisibility(TextView.VISIBLE);
    }

    @Override
    public void hideError() {
        txtVATError.setVisibility(TextView.GONE);
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

    @Override
    public void showError1() {
        txtBusinessNameError.setVisibility(TextView.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxBusinessName);
    }

    @Override
    public void showError2() {
        txtPhoneError.setVisibility(TextView.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxPhone);
    }

    @Override
    public void showError3() {
        txtVATError.setVisibility(TextView.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxVAT);
    }

    @Override
    public void hideError1() {
        txtBusinessNameError.setVisibility(TextView.GONE);
        ErrorBorder.removeStroke(tbxBusinessName);
    }

    @Override
    public void hideError2() {
        txtPhoneError.setVisibility(TextView.GONE);
        ErrorBorder.removeStroke(tbxPhone);
    }

    @Override
    public void hideError3() {
        txtVATError.setVisibility(TextView.GONE);
        ErrorBorder.removeStroke(tbxVAT);
    }

    private void configureSaveButton() {
        btnSave.setOnClickListener((view) -> {
            String name = String.valueOf(tbxBusinessName.getText());
            String phone = String.valueOf(tbxPhone.getText());
            String vat = String.valueOf(tbxVAT.getText());
            controller.handleSaveButton(name, phone, vat);
        });
    }

    private void configureCancelButton() {
        btnCancel.setOnClickListener((view) -> {
            controller.handleCancelButton();
        });
    }
}