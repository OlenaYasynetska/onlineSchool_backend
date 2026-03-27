package com.education.infrastructure.student;

import com.education.domain.student.Email;
import com.education.domain.student.SchoolId;
import com.education.domain.student.Student;
import com.education.domain.student.StudentId;
import com.education.domain.student.StudentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepositoryJpaAdapter implements StudentRepository {

    private final SpringDataStudentJpaRepository jpaRepository;

    public StudentRepositoryJpaAdapter(SpringDataStudentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Student save(Student student) {
        StudentJpaEntity entity = toEntity(student);
        StudentJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public Optional<Student> findById(StudentId studentId) {
        return jpaRepository.findById(studentId.value()).map(this::toDomain);
    }

    @Override
    public List<Student> findBySchoolId(SchoolId schoolId) {
        return jpaRepository.findBySchoolId(schoolId.value()).stream().map(this::toDomain).toList();
    }

    private StudentJpaEntity toEntity(Student student) {
        StudentJpaEntity entity = new StudentJpaEntity();
        entity.setId(student.id().value());
        entity.setFullName(student.fullName());
        entity.setEmail(student.email().value());
        entity.setSchoolId(student.schoolId().value());
        entity.setCreatedAt(student.createdAt());
        return entity;
    }

    private Student toDomain(StudentJpaEntity entity) {
        return Student.rehydrate(
                new StudentId(entity.getId()),
                entity.getFullName(),
                new Email(entity.getEmail()),
                new SchoolId(entity.getSchoolId()),
                entity.getCreatedAt()
        );
    }
}

