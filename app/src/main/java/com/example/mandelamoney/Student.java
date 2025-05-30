package com.example.mandelamoney;

import java.io.Serializable;

public class Student extends User implements Serializable {
    private String studentFirstName, studentLastName, studentNumber;
    public Student(String userEmail, String userPassword, double userBalance, String studentFirstName, String studentLastName, String studentNumber) {
        super(userEmail, userPassword, userBalance);
        this.studentFirstName = studentFirstName;
        this.studentLastName = studentLastName;
        this.studentNumber = studentNumber;
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
}
