package com.education.application.student;

import com.education.domain.student.Email;
import com.education.domain.student.SchoolId;
import com.education.domain.student.Student;
import com.education.domain.student.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateStudentService implements CreateStudentUseCase {

    private final StudentRepository studentRepository;

    public CreateStudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public StudentView execute(CreateStudentCommand command) {
        Email email = new Email(command.email());
        SchoolId schoolId = new SchoolId(command.schoolId());

        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateStudentEmailException(email.value());
        }

        Student student = Student.createNew(command.fullName(), email, schoolId, command.linkedUserId());
        Student saved = studentRepository.save(student);

        return new StudentView(
                saved.id().value(),
                saved.fullName(),
                saved.email().value(),
                saved.schoolId().value(),
                saved.createdAt()
        );
    }
}

