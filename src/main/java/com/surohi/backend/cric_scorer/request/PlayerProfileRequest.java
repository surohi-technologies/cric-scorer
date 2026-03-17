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

    // Optional
    private Integer battingIntentId;

    private String favouritePlayer;
    private String favouriteTeam;
    private String description;
}