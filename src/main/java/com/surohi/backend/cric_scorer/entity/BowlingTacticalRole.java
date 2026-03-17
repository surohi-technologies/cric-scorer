package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bowling_tactical_role", schema = "public")
@Getter
@Setter
public class BowlingTacticalRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    @Column
    private String description;
}
