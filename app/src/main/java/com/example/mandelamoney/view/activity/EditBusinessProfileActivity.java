package com.example.mandelamoney.view.activity;

import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private EditProfileController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_business_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        controller = new EditProfileController();
        connectToUi();
        configureSaveButton();
        configureCancelButton();

    }

    private void connectToUi() {
        tbxEmail = findViewById(R.id.tbx_email_editbusinessprofile);
        tbxBusinessName = findViewById(R.id.tbx_name_editbusinessprofile);
        tbxPhone = findViewById(R.id.tbx_phone_editbusinessprofile);
        tbxVAT = findViewById(R.id.tbx_vat_editbusinessprofile);
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

    }

    @Override
    public void hideError() {

    }

    private void configureSaveButton() {
        String email = String.valueOf(tbxEmail.getText());
        String name = String.valueOf(tbxBusinessName.getText());
        String phone = String.valueOf(tbxPhone.getText());
        String vat = String.valueOf(tbxVAT.getText());
        controller.handleSaveButton(email, name, phone, vat);
    }

    private void configureCancelButton() {

    }
}