package com.surohi.backend.cric_scorer.service.serviceImpl;

import com.surohi.backend.cric_scorer.constants.OtpChannel;
import com.surohi.backend.cric_scorer.constants.OtpPurpose;
import com.surohi.backend.cric_scorer.entity.PasswordResetToken;
import com.surohi.backend.cric_scorer.entity.UserDetail;
import com.surohi.backend.cric_scorer.repository.PasswordResetTokenRepository;
import com.surohi.backend.cric_scorer.repository.UserDetailRepository;
import com.surohi.backend.cric_scorer.response.ForgotPasswordStartResponse;
import com.surohi.backend.cric_scorer.response.ForgotPasswordVerifyResponse;
import com.surohi.backend.cric_scorer.service.OtpService;
import com.surohi.backend.cric_scorer.service.PasswordResetService;
import com.surohi.backend.cric_scorer.service.PasswordService;
import com.surohi.backend.cric_scorer.util.CryptoUtil;
import com.surohi.backend.cric_scorer.validator.ValidationError;
import com.surohi.backend.cric_scorer.validator.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserDetailRepository userDetailRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final OtpService otpService;
    private final PasswordService passwordService;
    private final long resetTokenExpirySeconds;
    private final String pepper;

    public PasswordResetServiceImpl(UserDetailRepository userDetailRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    OtpService otpService,
                                    PasswordService passwordService,
                                    @Value("${app.reset-token.expiry-seconds:600}") long resetTokenExpirySeconds,
                                    @Value("${app.otp.pepper:cric-scorer}") String pepper) {
        this.userDetailRepository = userDetailRepository;
        this.tokenRepository = tokenRepository;
        this.otpService = otpService;
        this.passwordService = passwordService;
        this.resetTokenExpirySeconds = Math.max(60, resetTokenExpirySeconds);
        this.pepper = pepper == null ? "cric-scorer" : pepper;
    }

    @Override
    @Transactional
    public ForgotPasswordStartResponse start(String loginId, OtpChannel preferredChannel) {
        // Generic response to avoid account enumeration.
        String message = "If an account exists for this login ID, we will send password reset OTP.";

        Optional<UserDetail> userOpt = findUserByLoginId(normalizeNullable(loginId));
        if (userOpt.isEmpty()) {
            return ForgotPasswordStartResponse.builder()
                    .message(message)
                    .availableChannels(List.of())
                    .sentChannel(null)
                    .build();
        }
        UserDetail user = userOpt.get();

        List<String> available = new ArrayList<>();
        if (hasEmail(user)) available.add(OtpChannel.EMAIL.name());
        if (hasPhone(user)) available.add(OtpChannel.PHONE.name());

        if (available.isEmpty()) {
            return ForgotPasswordStartResponse.builder()
                    .message(message)
                    .availableChannels(List.of())
                    .sentChannel(null)
                    .build();
        }

        OtpChannel sendCh = chooseChannel(user, preferredChannel);
        boolean sent = otpService.sendPasswordResetOtp(user, sendCh);
        return ForgotPasswordStartResponse.builder()
                .message(message)
                .availableChannels(available)
                .sentChannel(sent ? sendCh.name() : null)
                .build();
    }

    @Override
    @Transactional
    public ForgotPasswordVerifyResponse verifyOtp(String loginId, OtpChannel channel, String otp) {
        Optional<UserDetail> userOpt = findUserByLoginId(normalizeNullable(loginId));
        if (userOpt.isEmpty()) {
            throw new ValidationException("Validation failed", List.of(new ValidationError("otp", "Invalid OTP")));
        }
        UserDetail user = userOpt.get();
        boolean ok = otpService.verifyOtp(user.getId(), OtpPurpose.PASSWORD_RESET, channel, otp);
        if (!ok) {
            throw new ValidationException("Validation failed", List.of(new ValidationError("otp", "Invalid OTP")));
        }

        String raw = UUID.randomUUID().toString();
        Instant now = Instant.now();
        PasswordResetToken t = new PasswordResetToken();
        t.setUser(user);
        t.setTokenHash(CryptoUtil.sha256Hex(raw + ":" + pepper));
        t.setCreatedAt(now);
        t.setExpiresAt(now.plusSeconds(resetTokenExpirySeconds));
        tokenRepository.save(t);

        return ForgotPasswordVerifyResponse.builder()
                .message("OTP verified. Set a new password.")
                .resetToken(raw)
                .expiresInSeconds(resetTokenExpirySeconds)
                .build();
    }

    @Override
    @Transactional
    public void resetPassword(String resetToken, String newPassword, String confirmPassword) {
        List<ValidationError> errors = new ArrayList<>();
        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            errors.add(new ValidationError("confirmPassword", "confirmPassword must match newPassword"));
            throw new ValidationException("Validation failed", errors);
        }

        String hash = CryptoUtil.sha256Hex(normalizeNullable(resetToken) + ":" + pepper);
        PasswordResetToken t = tokenRepository.findActiveByHash(hash)
                .orElseThrow(() -> new ValidationException("Validation failed", List.of(new ValidationError("resetToken", "Invalid or expired reset token"))));

        Instant now = Instant.now();
        if (t.getExpiresAt() != null && now.isAfter(t.getExpiresAt())) {
            t.setConsumedAt(now);
            tokenRepository.save(t);
            throw new ValidationException("Validation failed", List.of(new ValidationError("resetToken", "Invalid or expired reset token")));
        }

        UserDetail user = t.getUser();
        passwordService.changePassword(user, newPassword);

        t.setConsumedAt(now);
        tokenRepository.save(t);
    }

    private Optional<UserDetail> findUserByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) return Optional.empty();
        if (loginId.contains("@")) {
            return userDetailRepository.findByEmailIdIgnoreCase(loginId);
        }
        if (isAllDigits(loginId)) {
            return userDetailRepository.findByPhoneNumber(loginId);
        }
        return userDetailRepository.findByUserNameIgnoreCase(loginId);
    }

    private static boolean isAllDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }

    private static String normalizeNullable(String v) {
        return v == null ? null : v.trim();
    }

    private static boolean hasEmail(UserDetail u) {
        String v = normalizeNullable(u.getEmailId());
        return v != null && !v.isBlank();
    }

    private static boolean hasPhone(UserDetail u) {
        String p = normalizeNullable(u.getPhoneNumber());
        String d = normalizeNullable(u.getPhoneCountryCode());
        return p != null && !p.isBlank() && d != null && !d.isBlank();
    }

    private static OtpChannel chooseChannel(UserDetail user, OtpChannel preferred) {
        if (preferred != null) {
            if (preferred == OtpChannel.EMAIL && hasEmail(user)) return preferred;
            if (preferred == OtpChannel.PHONE && hasPhone(user)) return preferred;
        }
        return hasEmail(user) ? OtpChannel.EMAIL : OtpChannel.PHONE;
    }
}

