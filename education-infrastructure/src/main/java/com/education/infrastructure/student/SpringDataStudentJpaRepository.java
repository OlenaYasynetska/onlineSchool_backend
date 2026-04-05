package com.education.infrastructure.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataStudentJpaRepository extends JpaRepository<StudentJpaEntity, String> {
    boolean existsByEmail(String email);

    Optional<StudentJpaEntity> findByUserId(String userId);

    List<StudentJpaEntity> findBySchoolId(String schoolId);

    List<StudentJpaEntity> findBySchoolIdOrderByCreatedAtAsc(String schoolId);

    long countBySchoolId(String schoolId);
}

