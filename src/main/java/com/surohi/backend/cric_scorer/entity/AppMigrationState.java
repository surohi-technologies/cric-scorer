package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "app_migration_state", schema = "public")
@Getter
@Setter
public class AppMigrationState {
    @Id
    private Integer id;

    @Column(name = "last_applied_version")
    private String lastAppliedVersion;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
