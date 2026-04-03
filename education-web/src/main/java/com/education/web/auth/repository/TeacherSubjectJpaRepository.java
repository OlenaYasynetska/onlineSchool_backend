package com.education.web.auth.repository;

import com.education.web.auth.model.TeacherSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherSubjectJpaRepository extends JpaRepository<TeacherSubjectEntity, String> {

    List<TeacherSubjectEntity> findByTeacher_IdOrderBySortOrderAsc(String teacherId);
}
