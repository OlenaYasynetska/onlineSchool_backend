package com.education.web.materials.repository;

import com.education.web.materials.model.StudyMaterialSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyMaterialSetJpaRepository extends JpaRepository<StudyMaterialSetEntity, String> {

    List<StudyMaterialSetEntity> findByTeacher_IdOrderByUpdatedAtDesc(String teacherId);

    List<StudyMaterialSetEntity> findBySchool_IdOrderByUpdatedAtDesc(String schoolId);
}
