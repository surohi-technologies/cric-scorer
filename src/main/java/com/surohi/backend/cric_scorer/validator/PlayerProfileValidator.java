package com.surohi.backend.cric_scorer.validator;

import com.surohi.backend.cric_scorer.request.PlayerProfileRequest;

import java.util.ArrayList;
import java.util.List;

public class PlayerProfileValidator {
    public void validate(PlayerProfileRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new ValidationError("request", "Request body cannot be null"));
            throw new ValidationException("Validation failed", errors);
        }

        if (request.getAliasName() == null || request.getAliasName().trim().isBlank()) {
            errors.add(new ValidationError("aliasName", "aliasName is required"));
        }
        if (request.getJerseyNumber() <= 0) {
            errors.add(new ValidationError("jerseyNumber", "jerseyNumber must be a positive integer"));
        }

        if (request.getBattingHandId() == null) {
            errors.add(new ValidationError("battingHandId", "battingHandId is required"));
        }
        if (request.getBowlingHandId() == null) {
            errors.add(new ValidationError("bowlingHandId", "bowlingHandId is required"));
        }
        if (request.getBattingStyleId() == null) {
            errors.add(new ValidationError("battingStyleId", "battingStyleId is required"));
        }
        if (request.getBattingPositionId() == null) {
            errors.add(new ValidationError("battingPositionId", "battingPositionId is required"));
        }
        if (request.getBowlingStyleId() == null) {
            errors.add(new ValidationError("bowlingStyleId", "bowlingStyleId is required"));
        }
        if (request.getBowlingTacticalRoleId() == null) {
            errors.add(new ValidationError("bowlingTacticalRoleId", "bowlingTacticalRoleId is required"));
        }
        if (request.getBowlingPreferenceId() == null) {
            errors.add(new ValidationError("bowlingPreferenceId", "bowlingPreferenceId is required"));
        }
        if (request.getPlayerRoleTypeId() == null) {
            errors.add(new ValidationError("playerRoleTypeId", "playerRoleTypeId is required"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }
}
