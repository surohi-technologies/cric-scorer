package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "batting_intent", schema = "public")
@Getter
@Setter
public class BattingIntent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "intent_name", nullable = false, unique = true, length = 50)
    private String intentName;

    @Column
    private String description;
}
