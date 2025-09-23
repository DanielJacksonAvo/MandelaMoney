package com.example.mandelamoney.view.activity;

import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mandelamoney.R;
import com.example.mandelamoney.model.Student;
import com.example.mandelamoney.util.UserSession;
import com.example.mandelamoney.view.Iface.IEditProfileView;

public class EditStudentProfileActivity extends AppCompatActivity implements IEditProfileView {
    EditText tbxEmail, tbxFirstName, tbxLastName, tbxStudentNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_student_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        connectToUi();
    }

    private void connectToUi() {
        tbxEmail = findViewById(R.id.tbx_email_editstudentprofile);
        tbxFirstName = findViewById(R.id.tbx_firstname_editstudentprofile);
        tbxLastName = findViewById(R.id.tbx_lastname_editstudentprofile);
        tbxStudentNumber = findViewById(R.id.tbx_studentnumber_editstudentprofile);
    }


    @Override
    public void loadUser() {
        tbxEmail.setText(UserSession.getUser().getUserEmail());
        tbxFirstName.setText(((Student)(UserSession.getUser())).getStudentFirstName());
        tbxLastName.setText(((Student)(UserSession.getUser())).getStudentLastName());
        tbxStudentNumber.setText(((Student)(UserSession.getUser())).getStudentNumber());
    }
}