package com.github.superz97.githubactivitytracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy  flywayMigrationStrategy() {
        return flyway -> {
            log.info("Starting Flyway migration...");
            log.info("Flyway baseline version: {}", flyway.getConfiguration().getBaselineVersion());
            flyway.repair();
            var result = flyway.migrate();
            log.info("Flyway migration completed successfully!");
            log.info("Migration count: {}", result.migrationsExecuted);
            log.info("Current version: {}", result.targetSchemaVersion);
            log.info("Success: {}", result.success);
        };
    }

}
