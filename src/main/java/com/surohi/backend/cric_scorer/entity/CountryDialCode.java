package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "code_country_dial_code",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_country_dial_code_iso2", columnNames = {"iso2"}),
                @UniqueConstraint(name = "uk_country_dial_code_dial_code", columnNames = {"dial_code"})
        }
)
@Getter
@Setter
public class CountryDialCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso2", nullable = false, length = 2)
    private String iso2;

    @Column(name = "country_name", nullable = false, length = 80)
    private String countryName;

    @Column(name = "dial_code", nullable = false, length = 8)
    private String dialCode; // ex: +91
}
