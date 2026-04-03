package com.education.web.auth.repository;

import com.education.web.auth.model.SubscriptionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanJpaRepository extends JpaRepository<SubscriptionPlanEntity, Integer> {
    Optional<SubscriptionPlanEntity> findByPlanKeyIgnoreCase(String planKey);
}

