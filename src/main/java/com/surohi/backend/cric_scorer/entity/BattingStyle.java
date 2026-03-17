package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "batting_style", schema = "public")
@Getter
@Setter
@Data
public class BattingStyle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String styleName;
}