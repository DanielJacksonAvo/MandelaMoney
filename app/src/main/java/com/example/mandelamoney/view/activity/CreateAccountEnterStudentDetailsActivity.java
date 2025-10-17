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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.CreateAccountController;
import com.example.mandelamoney.util.DataShare;
import com.example.mandelamoney.util.ErrorBorder;
import com.example.mandelamoney.view.Iface.ICreateStudentAccountView;

import java.util.concurrent.atomic.AtomicReference;

public class CreateAccountEnterStudentDetailsActivity extends AppCompatActivity implements ICreateStudentAccountView {

    private CreateAccountController controller;
    private TextView txtPasswordError, txtEmailError, txtFirstNameError, txtLastNameError, txtStudentNumberError;
    private EditText tbxFirstName, tbxLastName, tbxStudentNumber, tbxEmail, tbxPassword, tbxPasswordReenter;
    private ConstraintLayout loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account_enter_student_details);

        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        setController();
        connectToUI();

    }

    private void setController() {
        controller = (CreateAccountController) DataShare.receive();
        controller.setContextViewStudent(this, this);
    }

    private void connectToUI() {
        TextView btnCancel = findViewById(R.id.btn_cancel_editbusinessprofile);
        tbxEmail = findViewById(R.id.tbx_email_editbusinessprofile);
        tbxFirstName = findViewById(R.id.tbx_firstname_editstudentprofile);
        tbxLastName = findViewById(R.id.tbx_lastname_editstudentprofile);
        tbxStudentNumber = findViewById(R.id.tbx_studentnumber_editstudentprofile);
        Button btnCreateAccount = findViewById(R.id.btn_save_editbusinessprofile);
        tbxPassword = findViewById(R.id.tbx_password_createstudentaccount);
        tbxPasswordReenter = findViewById(R.id.tbx_password_reenter_createstudentaccount);
        ImageView imgPasswordIcon = findViewById(R.id.img_password_icon_createstudentaccount);
        ImageView imgPasswordRenterIcon = findViewById(R.id.img_password_reenter_createstudentaccount);
        txtPasswordError = findViewById(R.id.txt_password_error_createstudentaccount);
        txtEmailError = findViewById(R.id.txt_email_error_createstudentaccount);
        txtFirstNameError = findViewById(R.id.txt_firstname_error_createstudentaccount);
        txtLastNameError = findViewById(R.id.txt_lastname_error_createstudentaccount);
        txtStudentNumberError = findViewById(R.id.txt_studentnumber_error_createstudentaccount);
        loadingSpinner = findViewById(R.id.createstudentaccount_loading_spinner);
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
    public void showPasswordError(String message, boolean forPassword) {
        txtPasswordError.setVisibility(View.VISIBLE);
        txtPasswordError.setText(message);
        if (forPassword) {
            ErrorBorder.applyMandelaYellowBorder(tbxPassword);
            ErrorBorder.applyMandelaYellowBorder(tbxPasswordReenter);
        }

    }

    @Override
    public void hideEmailError() {
        txtEmailError.setVisibility(View.GONE);
        ErrorBorder.removeStroke(tbxEmail);
    }

    @Override
    public void hideFirstNameError() {
        txtFirstNameError.setVisibility(View.GONE);
        ErrorBorder.removeStroke(tbxFirstName);
    }

    @Override
    public void hideLastNameError() {
        txtLastNameError.setVisibility(View.GONE);
        ErrorBorder.removeStroke(tbxLastName);
    }

    @Override
    public void hideStudentNumberError() {
        txtStudentNumberError.setVisibility(View.GONE);
        ErrorBorder.removeStroke(tbxStudentNumber);
    }

    @Override
    public void hidePasswordError() {
        txtPasswordError.setVisibility(View.GONE);
        ErrorBorder.removeStroke(tbxPassword);
        ErrorBorder.removeStroke(tbxPasswordReenter);



    }

    @Override
    public void showLoadingSpinner() {
        loadingSpinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingSpinner() {
        loadingSpinner.setVisibility(View.GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showEmailError(String error) {
        txtEmailError.setText(error);
        txtEmailError.setVisibility(View.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxEmail);
    }

    @Override
    public void showFirstNameError() {
        txtFirstNameError.setVisibility(View.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxFirstName);
    }

    @Override
    public void showLastNameError() {
        txtLastNameError.setVisibility(View.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxLastName);
    }

    @Override
    public void showStudentNumberError() {
        txtStudentNumberError.setVisibility(View.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxStudentNumber);
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