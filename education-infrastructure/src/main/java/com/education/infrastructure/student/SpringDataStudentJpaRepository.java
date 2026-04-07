package com.education.infrastructure.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpringDataStudentJpaRepository extends JpaRepository<StudentJpaEntity, String> {
    boolean existsByEmail(String email);

    Optional<StudentJpaEntity> findByUserId(String userId);

    List<StudentJpaEntity> findBySchoolId(String schoolId);

    List<StudentJpaEntity> findBySchoolIdOrderByCreatedAtAsc(String schoolId);

    long countBySchoolId(String schoolId);

    @Query("SELECT s.schoolId, COUNT(s) FROM StudentJpaEntity s WHERE s.schoolId IN :ids GROUP BY s.schoolId")
    List<Object[]> countBySchoolIdsGrouped(@Param("ids") Collection<String> ids);
}

