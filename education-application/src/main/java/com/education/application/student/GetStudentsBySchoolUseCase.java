package com.education.application.student;

import java.util.List;

public interface GetStudentsBySchoolUseCase {
    List<StudentView> executeBySchoolId(String schoolId);
}

