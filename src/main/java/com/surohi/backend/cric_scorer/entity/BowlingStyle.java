package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bowling_style", schema = "public")
@Getter
@Setter
public class BowlingStyle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String styleName;
}