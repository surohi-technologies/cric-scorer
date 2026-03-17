package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bowling_preference", schema = "public")
@Getter
@Setter
public class BowlingPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_style_id", referencedColumnName = "id")
    private BowlingStyle bowlingStyle;

    @Column(nullable = false)
    private String preferenceName;
}
