package com.surohi.backend.cric_scorer.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@Entity
@Table(
        name = "user_detail",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_detail_email", columnNames = {"email_id"}),
                @UniqueConstraint(name = "uk_user_detail_phone", columnNames = {"phone_number"})
                ,
                @UniqueConstraint(name = "uk_user_detail_user_name", columnNames = {"user_name"}),
                @UniqueConstraint(name = "uk_user_detail_unique_identifier", columnNames = {"unique_identifier"})
        }
)
public class UserDetail extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String userName;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private String password;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "phone_country_code", length = 8)
    private String phoneCountryCode;

    @Column(name = "unique_identifier", nullable = false)
    private String uniqueIdentifier;

    private boolean isActive = true;
    private boolean isProfileCompleted = false;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = true;

    @Column(name = "verified_email", nullable = false)
    private boolean verifiedEmail = true;

    @Column(name = "verified_phone", nullable = false)
    private boolean verifiedPhone = true;

    @Column(name = "verification_required_channels")
    private String verificationRequiredChannels;
}
