package com.surohi.backend.cric_scorer.validator;

import java.util.ArrayList;
import java.util.List;

public class PasswordPolicyValidator {

    /**
     * Policy:
     * - length >= 8
     * - contains lower, upper, digit
     * - contains one of !$#@*&
     */
    public void validate(String field, String password, List<ValidationError> errors) {
        if (password == null || password.isBlank()) {
            errors.add(new ValidationError(field, "password is required"));
            return;
        }
        if (password.length() < 8) {
            errors.add(new ValidationError(field, "password must be at least 8 characters"));
        }
        if (!containsLower(password)) {
            errors.add(new ValidationError(field, "password must contain a lowercase letter"));
        }
        if (!containsUpper(password)) {
            errors.add(new ValidationError(field, "password must contain an uppercase letter"));
        }
        if (!containsDigit(password)) {
            errors.add(new ValidationError(field, "password must contain a digit"));
        }
        if (!containsAllowedSpecial(password)) {
            errors.add(new ValidationError(field, "password must contain a special character from !$#@*&"));
        }
    }

    public List<ValidationError> validatePasswordOnly(String password) {
        List<ValidationError> errors = new ArrayList<>();
        validate("password", password, errors);
        return errors;
    }

    private static boolean containsLower(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(s.charAt(i))) return true;
        }
        return false;
    }

    private static boolean containsUpper(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) return true;
        }
        return false;
    }

    private static boolean containsDigit(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) return true;
        }
        return false;
    }

    private static boolean containsAllowedSpecial(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '!' || c == '$' || c == '#' || c == '@' || c == '*' || c == '&') return true;
        }
        return false;
    }
}

