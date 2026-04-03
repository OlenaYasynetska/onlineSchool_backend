package com.education.web.auth.repository;

import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<UserEntity> findAllByRoleOrderByCreatedAtDesc(UserRole role);
}

