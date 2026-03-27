package com.education.infrastructure.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataStudentJpaRepository extends JpaRepository<StudentJpaEntity, String> {
    boolean existsByEmail(String email);

    List<StudentJpaEntity> findBySchoolId(String schoolId);
}

