package com.education.domain.student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    Student save(Student student);

    boolean existsByEmail(Email email);

    Optional<Student> findById(StudentId studentId);

    List<Student> findBySchoolId(SchoolId schoolId);
}

