package com.surohi.backend.cric_scorer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerProfileRequest {

    // Alias Name
    @NotBlank
    private String aliasName;

    @Positive
    private int jerseyNumber;

    @NotNull
    private Integer battingHandId;

    @NotNull
    private Integer bowlingHandId;

    @NotNull
    private Integer battingStyleId;

    @NotNull
    private Integer battingPositionId;

    @NotNull
    private Integer bowlingStyleId;

    @NotNull
    private Integer bowlingTacticalRoleId;

    @NotNull
    private Integer bowlingPreferenceId;

    @NotNull
    private Integer playerRoleTypeId;

    @NotNull
    private Integer battingIntentId;

    @NotBlank
    private String favouritePlayer;

    @NotBlank
    private String favouriteTeam;

    /**
     * If true (or null), backend will generate a consistent description based on selected options
     * and ignore any client-provided description. If false, backend will accept {@link #description}.
     */
    private Boolean autoDescription;

    private String description;
}
