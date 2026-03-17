package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "arm", schema = "public")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Arm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String armType;
}
