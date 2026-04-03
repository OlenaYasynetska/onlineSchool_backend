package com.education.web.auth.repository;

import com.education.web.auth.model.SchoolGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolGroupJpaRepository extends JpaRepository<SchoolGroupEntity, String> {

    List<SchoolGroupEntity> findByOrganization_IdOrderByCreatedAtDesc(String organizationId);

    Optional<SchoolGroupEntity> findByOrganization_IdAndCode(String organizationId, String code);
}
