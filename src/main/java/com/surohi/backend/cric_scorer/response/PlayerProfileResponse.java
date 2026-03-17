package com.surohi.backend.cric_scorer.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfileResponse {
    private String message;
    private Integer profileId;
    private Long userId;
    private String aliasName;
    private Integer jerseyNumber;
    private String graphicalMessage;
}
