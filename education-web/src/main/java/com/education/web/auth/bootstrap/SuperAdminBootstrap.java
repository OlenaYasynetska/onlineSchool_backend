package com.education.web.auth.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs after the context (and Flyway) is up so the super admin row exists before traffic.
 */
@Component
public class SuperAdminBootstrap implements ApplicationRunner {

    private final SuperAdminBootstrapService superAdminBootstrapService;

    public SuperAdminBootstrap(SuperAdminBootstrapService superAdminBootstrapService) {
        this.superAdminBootstrapService = superAdminBootstrapService;
    }

    @Override
    public void run(ApplicationArguments args) {
        superAdminBootstrapService.ensureSuperAdmin();
    }
}
