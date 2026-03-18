package com.surohi.backend.cric_scorer.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Za-z]+$", message = "firstName must contain letters only")
    private String firstName;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z]+$", message = "lastName must contain letters only")
    private String lastName;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotBlank
    private String gender;

    @Email
    private String emailId;

    // ex: +91
    private String phoneCountryCode;

    @Pattern(regexp = "^[0-9]{6,15}$", message = "phoneNumber must contain digits only")
    private String phoneNumber;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    @Size(min = 8, max = 72)
    private String confirmPassword;
}
