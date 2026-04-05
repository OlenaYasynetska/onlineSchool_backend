package com.education.application.student;

public record CreateStudentCommand(String fullName, String email, String schoolId, String linkedUserId) {
}

