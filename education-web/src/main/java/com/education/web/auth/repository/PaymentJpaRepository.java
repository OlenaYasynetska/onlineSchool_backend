package com.education.web.auth.repository;

import com.education.web.auth.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, String> {
    List<PaymentEntity> findAllByOrderByCreatedAtDesc();

    List<PaymentEntity> findAllByOrganization_IdOrderByCreatedAtDesc(
            String organizationId
    );
}

