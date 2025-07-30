package com.example.mandelamoney.view.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.CreateAccountController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.ICreateBusinessAccountView;

import java.util.concurrent.atomic.AtomicReference;

public class CreateAccountEnterBusinessDetailsActivity extends AppCompatActivity implements ICreateBusinessAccountView {

    private CreateAccountController controller;
    private TextView txtPasswordError;
    private TextView txtDetailError;

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
        EditText tbxEmail = findViewById(R.id.tbx_email_createbusinessaccount);
        EditText tbxName = findViewById(R.id.tbx_name_createbusinessaccount);
        EditText tbxVAT = findViewById(R.id.tbx_vat_createbusinessaccount);
        EditText tbxPhone = findViewById(R.id.tbx_phone_createbusinessaccount);
        txtPasswordError = findViewById(R.id.txt_error_password_createbusinessaccount);
        txtDetailError = findViewById(R.id.txt_error_details_createbusinessaccount);
        configureCancelButton(btnCancel);
        configurePasswordVisibility(imgPasswordIcon, tbxPassword);
        configurePasswordVisibility(imgPasswordRenterIcon, tbxPasswordReenter);
        configureCreateAccountButton(btnCreateAccount, tbxEmail, tbxName, tbxVAT, tbxPhone, tbxPassword, tbxPasswordReenter);
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> controller.handleCreateBusinessAccountCancel());
    }

    private void configureCreateAccountButton(Button btnCreateAccount, EditText tbxEmail, EditText tbxName, EditText tbxVAT, EditText tbxPhone, EditText tbxPassword, EditText tbxPasswordReenter) {
        btnCreateAccount.setOnClickListener((view) -> controller.handleCreateBusinessUser(String.valueOf(tbxEmail.getText()), String.valueOf(tbxName.getText()), String.valueOf(tbxVAT.getText()), String.valueOf(tbxPhone.getText()), String.valueOf(tbxPassword.getText()), String.valueOf(tbxPasswordReenter.getText())));
    }

    @Override
    public void showPasswordError(String message) {
        txtPasswordError.setText(message);
        txtPasswordError.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePasswordError() {
        txtPasswordError.setVisibility(View.GONE);
    }

    @Override
    public void showDetailError(String message) {
        txtDetailError.setText(message);
        txtDetailError.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideDetailError() {
        txtDetailError.setVisibility(View.GONE);
    }

    @Override
    public void finishActivity() {
        finish();
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
}