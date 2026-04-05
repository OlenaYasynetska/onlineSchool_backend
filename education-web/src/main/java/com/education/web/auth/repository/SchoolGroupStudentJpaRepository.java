package com.education.web.auth.repository;

import com.education.web.auth.model.SchoolGroupStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolGroupStudentJpaRepository extends JpaRepository<SchoolGroupStudentEntity, String> {

    boolean existsByStudentIdAndGroup_Id(String studentId, String groupId);

    List<SchoolGroupStudentEntity> findByGroup_Organization_Id(String organizationId);

    List<SchoolGroupStudentEntity> findByStudentId(String studentId);
}
