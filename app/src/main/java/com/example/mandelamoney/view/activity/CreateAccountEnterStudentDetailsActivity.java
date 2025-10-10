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
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.CreateAccountController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.view.Iface.ICreateStudentAccountView;

import java.util.concurrent.atomic.AtomicReference;

public class CreateAccountEnterStudentDetailsActivity extends AppCompatActivity implements ICreateStudentAccountView {

    private CreateAccountController controller;
    TextView txtPasswordError;
    TextView txtDetailError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account_enter_student_details);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

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
        controller.setContextViewStudent(this, this);
    }

    private void connectToUI() {
        TextView btnCancel = findViewById(R.id.btn_cancel_editstudentprofile);
        EditText tbxEmail = findViewById(R.id.tbx_email_editstudentprofile);
        EditText tbxFirstName = findViewById(R.id.tbx_firstname_editstudentprofile);
        EditText tbxLastName = findViewById(R.id.tbx_lastname_editstudentprofile);
        EditText tbxStudentNumber = findViewById(R.id.tbx_studentnumber_editstudentprofile);
        Button btnCreateAccount = findViewById(R.id.btn_save_editstudentprofile);
        EditText tbxPassword = findViewById(R.id.tbx_password_createstudentaccount);
        EditText tbxPasswordReenter = findViewById(R.id.tbx_password_reenter_createstudentaccount);
        ImageView imgPasswordIcon = findViewById(R.id.img_password_icon_createstudentaccount);
        ImageView imgPasswordRenterIcon = findViewById(R.id.img_password_reenter_createstudentaccount);
        txtPasswordError = findViewById(R.id.txt_error_password_createstudentaccount);
        txtDetailError = findViewById(R.id.txt_error_details_createstudentaccount);
        configureCancelButton(btnCancel);
        configureCreateAccountButton(btnCreateAccount, tbxEmail, tbxFirstName, tbxLastName, tbxStudentNumber, tbxPassword, tbxPasswordReenter);
        configurePasswordVisibility(imgPasswordIcon, tbxPassword);
        configurePasswordVisibility(imgPasswordRenterIcon, tbxPasswordReenter);
    }

    private void configureCancelButton(TextView btnCancel) {
        btnCancel.setOnClickListener((view) -> controller.handleCreateStudentAccountCancel());
    }

    private void configureCreateAccountButton(Button btnCreateAccount, EditText tbxEmail, EditText tbxFirstName, EditText tbxLastName, EditText tbxStudentNumber, EditText tbxPassword, EditText tbxPasswordReenter) {
        btnCreateAccount.setOnClickListener((view) -> controller.handleCreateStudentUser(String.valueOf(tbxEmail.getText()), String.valueOf(tbxFirstName.getText()), String.valueOf(tbxLastName.getText()), String.valueOf(tbxStudentNumber.getText()), String.valueOf(tbxPassword.getText()), String.valueOf(tbxPasswordReenter.getText())));
    }

    @Override
    public void showPasswordError(String message) {
        txtPasswordError.setVisibility(View.VISIBLE);
        txtPasswordError.setText(message);
    }

    @Override
    public void hidePasswordError() {
        txtPasswordError.setVisibility(View.GONE);

    }

    @Override
    public void showDetailError(String message) {
        txtDetailError.setVisibility(View.VISIBLE);
        txtDetailError.setText(message);
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