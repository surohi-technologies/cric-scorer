package com.surohi.backend.cric_scorer.controller;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import com.surohi.backend.cric_scorer.request.ForgotPasswordRequest;
import com.surohi.backend.cric_scorer.request.ForgotPasswordVerifyOtpRequest;
import com.surohi.backend.cric_scorer.request.LoginRequest;
import com.surohi.backend.cric_scorer.request.ResetPasswordRequest;
import com.surohi.backend.cric_scorer.response.ForgotPasswordStartResponse;
import com.surohi.backend.cric_scorer.response.ForgotPasswordVerifyResponse;
import com.surohi.backend.cric_scorer.response.LoginResponse;
import com.surohi.backend.cric_scorer.response.LogoutResponse;
import com.surohi.backend.cric_scorer.response.MessageResponse;
import com.surohi.backend.cric_scorer.service.AuthService;
import com.surohi.backend.cric_scorer.service.PasswordResetService;
import com.surohi.backend.cric_scorer.service.SessionService;
import com.surohi.backend.cric_scorer.validator.ValidationError;
import com.surohi.backend.cric_scorer.validator.ValidationException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(CricScorerServiceConstants.BASE_URL)
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, SessionService sessionService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping(CricScorerServiceConstants.AUTH_LOGIN_URL)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping(CricScorerServiceConstants.AUTH_LOGOUT_URL)
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request) {
        String sessionKey = extractSessionKey(request);
        if (sessionKey == null || sessionKey.isBlank()) {
            return ResponseEntity.status(401).body(LogoutResponse.builder().message("Missing session key").build());
        }
        sessionService.logout(sessionKey);
        return ResponseEntity.ok(LogoutResponse.builder().message("Logged out successfully").build());
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<ForgotPasswordStartResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        var preferred = parseOptionalChannel(request.getChannel());
        return ResponseEntity.ok(passwordResetService.start(request.getLoginId(), preferred));
    }

    @PostMapping("/auth/forgot-password/verify-otp")
    public ResponseEntity<ForgotPasswordVerifyResponse> forgotPasswordVerifyOtp(@Valid @RequestBody ForgotPasswordVerifyOtpRequest request) {
        var channel = parseRequiredChannel(request.getChannel());
        return ResponseEntity.ok(passwordResetService.verifyOtp(request.getLoginId(), channel, request.getOtp()));
    }

    @PostMapping("/auth/forgot-password/reset")
    public ResponseEntity<MessageResponse> forgotPasswordReset(HttpServletRequest httpRequest,
                                                              @Valid @RequestBody ResetPasswordRequest request) {
        String token = httpRequest.getHeader("X-Reset-Token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(401).body(MessageResponse.builder().message("Missing reset token").build());
        }
        passwordResetService.resetPassword(token.trim(), request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(MessageResponse.builder().message("Password updated successfully").build());
    }

    private static com.surohi.backend.cric_scorer.constants.OtpChannel parseOptionalChannel(String value) {
        if (value == null || value.isBlank()) return null;
        return parseRequiredChannel(value);
    }

    private static com.surohi.backend.cric_scorer.constants.OtpChannel parseRequiredChannel(String value) {
        try {
            return com.surohi.backend.cric_scorer.constants.OtpChannel.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new ValidationException("Validation failed", java.util.List.of(new ValidationError("channel", "channel must be EMAIL or PHONE")));
        }
    }

    private static String extractSessionKey(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring("Bearer ".length()).trim();
        }
        String header = request.getHeader("X-Session-Key");
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        return null;
    }
}
