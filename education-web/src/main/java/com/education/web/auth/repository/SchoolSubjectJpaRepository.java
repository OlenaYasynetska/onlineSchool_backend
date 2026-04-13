package com.education.web.auth.repository;

import com.education.web.auth.model.SchoolSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolSubjectJpaRepository extends JpaRepository<SchoolSubjectEntity, String> {

    List<SchoolSubjectEntity> findByOrganization_IdOrderByTitleAsc(String organizationId);

    long countByOrganization_Id(String organizationId);

    Optional<SchoolSubjectEntity> findByIdAndOrganization_Id(String id, String organizationId);

    Optional<SchoolSubjectEntity> findByOrganization_IdAndTitleIgnoreCase(
            String organizationId,
            String title
    );
}
