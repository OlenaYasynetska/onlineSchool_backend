package com.education.web.auth.repository;

import com.education.web.auth.model.SchoolGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolGroupJpaRepository extends JpaRepository<SchoolGroupEntity, String> {

    List<SchoolGroupEntity> findByOrganization_IdOrderByCreatedAtDesc(String organizationId);

    /** Старі групи першими (порядок 1, 2, 3… на картках). */
    List<SchoolGroupEntity> findByOrganization_IdOrderByCreatedAtAsc(String organizationId);

    Optional<SchoolGroupEntity> findByOrganization_IdAndCode(String organizationId, String code);

    /** Групи, де цей викладач призначений (колонка {@code school_groups.teacher_id}). */
    List<SchoolGroupEntity> findByTeacher_IdOrderByNameAsc(String teacherId);
}
