package com.education.web.auth.repository;

import com.education.web.auth.model.TeacherSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherSubjectJpaRepository extends JpaRepository<TeacherSubjectEntity, String> {

    List<TeacherSubjectEntity> findByTeacher_IdOrderBySortOrderAsc(String teacherId);

    Optional<TeacherSubjectEntity> findByIdAndTeacher_Id(String id, String teacherId);
}
