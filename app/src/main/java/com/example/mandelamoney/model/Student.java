package com.example.mandelamoney.model;

import java.io.Serializable;

public class Student extends User implements Serializable {
    private String studentFirstName, studentLastName, studentNumber;

    public Student(String userEmail) {
        super(userEmail);
    }

    public String getStudentFirstName() {
        return studentFirstName;
    }

    public void setStudentFirstName(String studentFirstName) {
        this.studentFirstName = studentFirstName;
    }

    public String getStudentLastName() {
        return studentLastName;
    }

    public void setStudentLastName(String studentLastName) {
        this.studentLastName = studentLastName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getStudentFullName() {
        return studentFirstName + " " + studentLastName;
    }
}
