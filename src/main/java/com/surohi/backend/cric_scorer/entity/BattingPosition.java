package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "batting_position", schema = "public")
@Getter
@Setter
public class BattingPosition
{
    @Id
    public int id;
    public String Position;
    public String Role;
}