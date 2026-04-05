package com.education.application.student;

import com.education.domain.student.SchoolId;
import com.education.domain.student.Student;
import com.education.domain.student.StudentId;
import com.education.domain.student.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetStudentService implements GetStudentByIdUseCase, GetStudentsBySchoolUseCase {

    private final StudentRepository studentRepository;

    public GetStudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public StudentView execute(String studentId) {
        Student student = studentRepository
                .findById(new StudentId(studentId))
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        return toView(student);
    }

    @Override
    public List<StudentView> executeBySchoolId(String schoolId) {
        return studentRepository
                .findBySchoolId(new SchoolId(schoolId))
                .stream()
                .map(this::toView)
                .toList();
    }

    private StudentView toView(Student student) {
        return new StudentView(
                student.id().value(),
                student.fullName(),
                student.email().value(),
                student.schoolId().value(),
                student.createdAt(),
                false
        );
    }
}

