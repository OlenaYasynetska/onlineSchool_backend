package com.education.web.auth.repository;

import com.education.web.auth.model.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, String> {
    List<OrganizationEntity> findAllByOrderByRegisteredAtDesc();

    Optional<OrganizationEntity> findByAdminUserId(String adminUserId);
}

