package com.surohi.backend.cric_scorer.service.serviceImpl;

import com.surohi.backend.cric_scorer.entity.UserDetail;
import com.surohi.backend.cric_scorer.repository.CountryDialCodeRepository;
import com.surohi.backend.cric_scorer.repository.UserDetailRepository;
import com.surohi.backend.cric_scorer.request.UserRegistrationRequest;
import com.surohi.backend.cric_scorer.response.UserRegistrationResponse;
import com.surohi.backend.cric_scorer.service.OtpService;
import com.surohi.backend.cric_scorer.service.PasswordService;
import com.surohi.backend.cric_scorer.service.UserDetailService;
import com.surohi.backend.cric_scorer.validator.UserRegistrationValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserDetailServiceImpl implements UserDetailService {

    private final UserDetailRepository userDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRegistrationValidator userRegistrationValidator;
    private final OtpService otpService;
    private final PasswordService passwordService;
    private final boolean requireBothChannels;

    public UserDetailServiceImpl(UserDetailRepository userDetailRepository,
                                 PasswordEncoder passwordEncoder,
                                 CountryDialCodeRepository countryDialCodeRepository,
                                 OtpService otpService,
                                 PasswordService passwordService,
                                 @Value("${app.registration.require-both-channels:true}") boolean requireBothChannels) {
        this.userDetailRepository = userDetailRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRegistrationValidator = new UserRegistrationValidator(countryDialCodeRepository);
        this.otpService = otpService;
        this.passwordService = passwordService;
        this.requireBothChannels = requireBothChannels;
    }

    @Override
    public ResponseEntity<UserRegistrationResponse> register(UserRegistrationRequest request) {
        userRegistrationValidator.validate(request);

        String email = normalizeNullable(request.getEmailId());
        String phone = normalizeNullable(request.getPhoneNumber());
        String phoneCountryCode = normalizeNullable(request.getPhoneCountryCode());

        if (email != null && !email.isBlank() && userDetailRepository.existsByEmailIdIgnoreCase(email)) {
            return ResponseEntity.status(409).body(UserRegistrationResponse.builder()
                    .message("Email already registered")
                    .build());
        }

        if (phone != null && !phone.isBlank() && userDetailRepository.existsByPhoneNumber(phone)) {
            return ResponseEntity.status(409).body(UserRegistrationResponse.builder()
                    .message("Phone number already registered")
                    .build());
        }

        UserDetail user = new UserDetail();
        user.setUserName(generateUniqueUserName(request.getFirstName(), request.getLastName()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setGender(request.getGender().trim());
        user.setDob(request.getDob());
        user.setEmailId(email != null && !email.isBlank() ? email.trim() : null);
        user.setPhoneNumber(phone != null && !phone.isBlank() ? phone.trim() : null);
        user.setPhoneCountryCode(phone != null && !phone.isBlank() ? phoneCountryCode : null);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUniqueIdentifier(UUID.randomUUID().toString());
        // New users must verify OTP before login
        user.setActive(false);
        user.setProfileCompleted(false);
        user.setVerified(false);
        user.setVerifiedEmail(false);
        user.setVerifiedPhone(false);

        // Track which channel(s) need verification.
        String channels = buildRequiredChannels(email, phone);
        user.setVerificationRequiredChannels(channels);

        UserDetail saved = userDetailRepository.save(user);

        // record initial password in history
        passwordService.recordInitialPassword(saved);

        // send OTP(s)
        var sent = otpService.sendRegistrationOtps(saved);

        return ResponseEntity.ok(UserRegistrationResponse.builder()
                .message("User registered successfully. Please verify OTP to activate your account.")
                .userId(saved.getId())
                .userName(saved.getUserName())
                .uniqueIdentifier(saved.getUniqueIdentifier())
                .verificationRequired(true)
                .requiredChannels(sent.stream().map(Enum::name).toList())
                .build());
    }

    private String buildRequiredChannels(String email, String phone) {
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPhone = phone != null && !phone.isBlank();
        if (hasEmail && hasPhone) {
            return requireBothChannels ? "EMAIL,PHONE" : "EMAIL";
        }
        if (hasEmail) return "EMAIL";
        if (hasPhone) return "PHONE";
        return null;
    }

    private String generateUniqueUserName(String firstName, String lastName) {
        String base = (firstName == null ? "" : firstName.trim())
                + "."
                + (lastName == null ? "" : lastName.trim());
        base = base.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9.]", "");
        if (base.length() > 24) {
            base = base.substring(0, 24);
        }
        if (base.isBlank() || ".".equals(base)) {
            base = "player";
        }

        // Try a few times with random suffixes; collisions should be rare.
        for (int i = 0; i < 20; i++) {
            String candidate = base + randomDigits(4);
            if (!userDetailRepository.existsByUserNameIgnoreCase(candidate)) {
                return candidate;
            }
        }

        // Fallback: highly unique suffix.
        String fallback = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        return fallback.length() > 32 ? fallback.substring(0, 32) : fallback;
    }

    private String randomDigits(int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    private static String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
