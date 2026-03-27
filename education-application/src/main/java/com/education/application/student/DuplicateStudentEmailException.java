package com.education.application.student;

public class DuplicateStudentEmailException extends RuntimeException {
    public DuplicateStudentEmailException(String email) {
        super("Student with email already exists: " + email);
    }
}

