package com.surohi.backend.cric_scorer.config;

import com.surohi.backend.cric_scorer.entity.AppMigrationState;
import com.surohi.backend.cric_scorer.repository.AppMigrationStateRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class FlywayVersionTracker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FlywayVersionTracker.class);

    private final ObjectProvider<Flyway> flywayProvider;
    private final AppMigrationStateRepository stateRepository;

    public FlywayVersionTracker(ObjectProvider<Flyway> flywayProvider,
                               AppMigrationStateRepository stateRepository) {
        this.flywayProvider = flywayProvider;
        this.stateRepository = stateRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Flyway flyway = flywayProvider.getIfAvailable();
        if (flyway == null) {
            log.warn("Flyway bean not found; migrations were not executed. Check Flyway dependencies and configuration.");
            return;
        }

        // Ensure migrations are applied, then report what happened.
        flyway.migrate();

        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        String version = current != null && current.getVersion() != null ? current.getVersion().getVersion() : null;

        log.info("Flyway current version: {}", version == null ? "(none)" : version);
        for (MigrationInfo applied : info.applied()) {
            log.info("Flyway applied: {} | {} | {}", safe(applied.getVersion()), safe(applied.getDescription()), safe(applied.getScript()));
        }
        for (MigrationInfo pending : info.pending()) {
            log.info("Flyway pending: {} | {} | {}", safe(pending.getVersion()), safe(pending.getDescription()), safe(pending.getScript()));
        }

        AppMigrationState state = stateRepository.findById(1).orElseGet(() -> {
            AppMigrationState s = new AppMigrationState();
            s.setId(1);
            return s;
        });
        state.setLastAppliedVersion(version);
        state.setUpdatedAt(Instant.now());
        stateRepository.save(state);
    }

    private static String safe(Object value) {
        return value == null ? "" : value.toString();
    }
}