package com.education.web.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Перед {@link Flyway#migrate()} завжди викликає {@link Flyway#repair()}, щоб прибрати
 * failed-записи в {@code flyway_schema_history} (наприклад після обірваної V10).
 * <p>
 * Лише YAML {@code spring.flyway.repair-on-migrate} інколи недостатньо залежно від порядку
 * ініціалізації; явний {@link FlywayMigrationStrategy} гарантує порядок repair → migrate.
 */
@Configuration
@ConditionalOnClass(Flyway.class)
public class FlywayRepairBeforeMigrateConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
