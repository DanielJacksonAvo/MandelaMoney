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
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IEditProfileView;

public class EditStudentProfileActivity extends AppCompatActivity implements IEditProfileView {
    private EditText tbxEmail, tbxFirstName, tbxLastName, tbxStudentNumber;
    private TextView txtError, btnCancel;
    private Button btnSave;
    private ConstraintLayout loadingSpinner;
    private EditProfileController controller;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_student_profile);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        controller = new EditProfileController(this, this);
        connectToUi();
        configureSaveButton();
        configureCancelButton();
    }

    private void connectToUi() {
        tbxEmail = findViewById(R.id.tbx_email_editstudentprofile);
        tbxFirstName = findViewById(R.id.tbx_firstname_editstudentprofile);
        tbxLastName = findViewById(R.id.tbx_lastname_editstudentprofile);
        tbxStudentNumber = findViewById(R.id.tbx_studentnumber_editstudentprofile);
        txtError = findViewById(R.id.txt_error_details_editstudentprofile);
        btnCancel = findViewById(R.id.btn_cancel_editstudentprofile);
        btnSave = findViewById(R.id.btn_save_editstudentprofile);
        loadingSpinner = findViewById(R.id.editstudentprofile_loading_spinner);
    }


    @Override
    public void loadUser() {
        tbxEmail.setText(UserSession.getUser().getUserEmail());
        tbxFirstName.setText(((Student)(UserSession.getUser())).getStudentFirstName());
        tbxLastName.setText(((Student)(UserSession.getUser())).getStudentLastName());
        tbxStudentNumber.setText(((Student)(UserSession.getUser())).getStudentNumber());
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
            String firstName = String.valueOf(tbxFirstName.getText());
            String lastName = String.valueOf(tbxLastName.getText());
            String studentNumber = String.valueOf(tbxStudentNumber.getText());
            controller.handleSaveButton(email, firstName, lastName, studentNumber);
        });

    }

    private void configureCancelButton() {
        btnCancel.setOnClickListener((view) -> {
            controller.handleCancelButton();
        });
    }
}