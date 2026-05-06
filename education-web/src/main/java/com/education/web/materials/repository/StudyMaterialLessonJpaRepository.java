package com.education.web.materials.repository;

import com.education.web.materials.model.StudyMaterialLessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyMaterialLessonJpaRepository extends JpaRepository<StudyMaterialLessonEntity, String> {

    List<StudyMaterialLessonEntity> findByMaterialSet_IdOrderBySortOrderAsc(String materialSetId);

    long countByMaterialSet_Id(String materialSetId);
}
