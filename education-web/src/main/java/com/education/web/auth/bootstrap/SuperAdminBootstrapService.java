package com.education.web.auth.bootstrap;

import com.education.web.auth.model.UserEntity;
import com.education.web.auth.model.UserRole;
import com.education.web.auth.repository.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Creates or updates the platform super admin from {@code SUPER_ADMIN_PASSWORD} (server-side only).
 * Skipped when the password env is unset — use local frontend bypass or set the variable.
 */
@Service
public class SuperAdminBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrapService.class);

    private final UserJpaRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String bootstrapPassword;

    public SuperAdminBootstrapService(
            UserJpaRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.super-admin.email}") String email,
            @Value("${app.super-admin.bootstrap-password:}") String bootstrapPassword
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Transactional
    public void ensureSuperAdmin() {
        String plain = bootstrapPassword == null ? "" : bootstrapPassword.trim();
        if (plain.isEmpty()) {
            log.debug(
                    "Super admin bootstrap skipped (set SUPER_ADMIN_PASSWORD to create or sync password)"
            );
            return;
        }
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (normalizedEmail.isEmpty()) {
            log.warn("Super admin bootstrap skipped (empty app.super-admin.email)");
            return;
        }

        Optional<UserEntity> existing = users.findByEmailIgnoreCase(normalizedEmail);
        if (existing.isPresent()) {
            UserEntity user = existing.get();
            if (user.getRole() != UserRole.SUPER_ADMIN) {
                log.warn(
                        "Super admin bootstrap skipped: email {} already exists with role {}",
                        normalizedEmail,
                        user.getRole()
                );
                return;
            }
            user.setPasswordHash(passwordEncoder.encode(plain));
            user.setEnabled(true);
            users.save(user);
            log.info("Super admin password synced from environment for {}", normalizedEmail);
            return;
        }

        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(plain));
        user.setFirstName("Super");
        user.setLastName("Admin");
        user.setRole(UserRole.SUPER_ADMIN);
        user.setEnabled(true);
        users.save(user);
        log.info("Super admin user created: {}", normalizedEmail);
    }
}
