package com.surohi.backend.cric_scorer.validator;

import com.surohi.backend.cric_scorer.repository.CountryDialCodeRepository;
import com.surohi.backend.cric_scorer.request.UserRegistrationRequest;

import java.util.ArrayList;
import java.util.List;

public class UserRegistrationValidator {
    private final CountryDialCodeRepository countryDialCodeRepository;
    private final PasswordPolicyValidator passwordPolicyValidator = new PasswordPolicyValidator();

    public UserRegistrationValidator(CountryDialCodeRepository countryDialCodeRepository) {
        this.countryDialCodeRepository = countryDialCodeRepository;
    }

    public void validate(UserRegistrationRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new ValidationError("request", "Request body cannot be null"));
            throw new ValidationException("Validation failed", errors);
        }

        String email = normalizeNullable(request.getEmailId());
        String phone = normalizeNullable(request.getPhoneNumber());
        String dial = normalizeNullable(request.getPhoneCountryCode());

        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            errors.add(new ValidationError("emailId", "Either emailId or phoneNumber is required"));
            errors.add(new ValidationError("phoneNumber", "Either emailId or phoneNumber is required"));
        }

        if (phone != null && !phone.isBlank()) {
            if (!isAllDigits(phone)) {
                errors.add(new ValidationError("phoneNumber", "phoneNumber must contain digits only"));
            }
            if (dial == null || dial.isBlank()) {
                errors.add(new ValidationError("phoneCountryCode", "phoneCountryCode is required when phoneNumber is provided"));
            } else if (!isValidDialCodeFormat(dial)) {
                errors.add(new ValidationError("phoneCountryCode", "phoneCountryCode must look like +91"));
            } else {
                // If a dial-code table is seeded, enforce the selection. If empty, allow any valid format.
                long count = countryDialCodeRepository.count();
                if (count > 0 && !countryDialCodeRepository.existsByDialCode(dial)) {
                    errors.add(new ValidationError("phoneCountryCode", "Unsupported phoneCountryCode"));
                }
            }
        }

        // Password checks
        String password = normalizeNullable(request.getPassword());
        String confirm = normalizeNullable(request.getConfirmPassword());
        passwordPolicyValidator.validate("password", password, errors);
        if (confirm == null || confirm.isBlank()) {
            errors.add(new ValidationError("confirmPassword", "confirmPassword is required"));
        } else if (password != null && !password.equals(confirm)) {
            errors.add(new ValidationError("confirmPassword", "confirmPassword must match password"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private static boolean isValidDialCodeFormat(String dial) {
        if (!dial.startsWith("+")) {
            return false;
        }
        if (dial.length() < 2 || dial.length() > 5) {
            return false;
        }
        for (int i = 1; i < dial.length(); i++) {
            if (!Character.isDigit(dial.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAllDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return !value.isEmpty();
    }

    private static String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
