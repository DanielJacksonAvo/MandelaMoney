package com.example.mandelamoney.view.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.mandelamoney.R;
import com.example.mandelamoney.controller.EditProfileController;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.util.ErrorBorder;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IEditProfileView;

public class EditStudentProfileActivity extends AppCompatActivity implements IEditProfileView {
    private EditText tbxFirstName, tbxLastName, tbxStudentNumber;
    private TextView txtErrorFirstName, txtErrorLastName, txtErrorStudentNumber, btnCancel;
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
        connectToUi();
        controller = new EditProfileController(this, this);
        configureSaveButton();
        configureCancelButton();
    }

    private void connectToUi() {
        tbxFirstName = findViewById(R.id.tbx_firstname_editstudentprofile);
        tbxLastName = findViewById(R.id.tbx_lastname_editstudentprofile);
        tbxStudentNumber = findViewById(R.id.tbx_studentnumber_editstudentprofile);
        btnCancel = findViewById(R.id.btn_cancel_editbusinessprofile);
        btnSave = findViewById(R.id.btn_save_editbusinessprofile);
        loadingSpinner = findViewById(R.id.editstudentprofile_loading_spinner);
        txtErrorFirstName = findViewById(R.id.txt_firstname_error_editstudentprofile);
        txtErrorLastName = findViewById(R.id.txt_lastname_error_editstudentprofile);
        txtErrorStudentNumber = findViewById(R.id.txt_studentnumber_error_editstudentprofile);
    }


    @Override
    public void loadUser() {
        tbxFirstName.setText(((Student)(UserSession.getUser())).getStudentFirstName());
        tbxLastName.setText(((Student)(UserSession.getUser())).getStudentLastName());
        tbxStudentNumber.setText(((Student)(UserSession.getUser())).getStudentNumber());
    }

    @Override
    public void showError(String error) {
        txtErrorStudentNumber.setText(error);
        txtErrorStudentNumber.setVisibility(TextView.VISIBLE);
    }

    @Override
    public void hideError() {
        txtErrorStudentNumber.setVisibility(TextView.GONE);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void showLoadingScreen() {
        if (loadingSpinner != null) {
            loadingSpinner.setVisibility(ConstraintLayout.VISIBLE);
        }
    }

    @Override
    public void hideLoadingScreen() {
        if (loadingSpinner != null) {
            loadingSpinner.setVisibility(ConstraintLayout.GONE);
        }
    }

    @Override
    public void showError1() {
        txtErrorFirstName.setVisibility(TextView.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxFirstName);
    }

    @Override
    public void showError2() {
        txtErrorLastName.setVisibility(TextView.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxLastName);
    }

    @Override
    public void showError3() {
        txtErrorStudentNumber.setText(R.string.invalid_student_number);
        txtErrorStudentNumber.setVisibility(TextView.VISIBLE);
        ErrorBorder.applyMandelaYellowBorder(tbxStudentNumber);
    }

    @Override
    public void hideError1() {
        txtErrorFirstName.setVisibility(TextView.GONE);
        ErrorBorder.removeStroke(tbxFirstName);
    }

    @Override
    public void hideError2() {
        txtErrorLastName.setVisibility(TextView.GONE);
        ErrorBorder.removeStroke(tbxLastName);
    }

    @Override
    public void hideError3() {
        txtErrorStudentNumber.setVisibility(TextView.GONE);
        ErrorBorder.removeStroke(tbxStudentNumber);
    }

    private void configureSaveButton() {
        btnSave.setOnClickListener((view) -> {
            String firstName = String.valueOf(tbxFirstName.getText());
            String lastName = String.valueOf(tbxLastName.getText());
            String studentNumber = String.valueOf(tbxStudentNumber.getText());
            controller.handleSaveButton(firstName, lastName, studentNumber);
        });

    }

    private void configureCancelButton() {
        btnCancel.setOnClickListener((view) -> {
            controller.handleCancelButton();
        });
    }
}