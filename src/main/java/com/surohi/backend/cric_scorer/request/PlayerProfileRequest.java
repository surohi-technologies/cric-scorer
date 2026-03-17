package com.surohi.backend.cric_scorer.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlayerProfileRequest {
    @NotNull
    private Long userId;

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
