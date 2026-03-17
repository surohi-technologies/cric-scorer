package com.surohi.backend.cric_scorer.entity;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
public class Base {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = CricScorerServiceConstants.CREATED_AT, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = CricScorerServiceConstants.UPDATED_AT)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
