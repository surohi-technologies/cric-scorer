package com.surohi.backend.cric_scorer.validator;

import com.surohi.backend.cric_scorer.request.LoginRequest;

import java.util.ArrayList;
import java.util.List;

public class LoginValidator {
    public void validate(LoginRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new ValidationError("request", "Request body cannot be null"));
            throw new ValidationException("Validation failed", errors);
        }

        if (request.getLoginId() == null || request.getLoginId().trim().isBlank()) {
            errors.add(new ValidationError("loginId", "loginId is required"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            errors.add(new ValidationError("password", "password is required"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }
}

