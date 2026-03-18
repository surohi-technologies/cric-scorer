package com.surohi.backend.cric_scorer.service.serviceImpl;

import com.surohi.backend.cric_scorer.repository.PlayerProfileRepository;
import com.surohi.backend.cric_scorer.repository.UserDetailRepository;
import com.surohi.backend.cric_scorer.request.LoginRequest;
import com.surohi.backend.cric_scorer.response.LoginResponse;
import com.surohi.backend.cric_scorer.service.AuthService;
import com.surohi.backend.cric_scorer.service.SessionService;
import com.surohi.backend.cric_scorer.validator.LoginValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDetailRepository userDetailRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final long idleTimeoutSeconds;
    private final LoginValidator loginValidator = new LoginValidator();

    public AuthServiceImpl(UserDetailRepository userDetailRepository,
                           PlayerProfileRepository playerProfileRepository,
                           PasswordEncoder passwordEncoder,
                           SessionService sessionService,
                           @Value("${app.session.idle-timeout-seconds:60}") long idleTimeoutSeconds) {
        this.userDetailRepository = userDetailRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.idleTimeoutSeconds = Math.max(1, idleTimeoutSeconds);
    }

    @Override
    @Transactional
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        loginValidator.validate(request);

        String loginId = normalizeNullable(request.getLoginId());
        String password = request.getPassword();

        Optional<com.surohi.backend.cric_scorer.entity.UserDetail> userOpt = findUserByLoginId(loginId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(LoginResponse.builder().message("Invalid credentials").build());
        }

        var user = userOpt.get();
        if (!user.isActive() || !user.isVerified()) {
            return ResponseEntity.status(403).body(LoginResponse.builder()
                    .message("Account is not verified. Please complete OTP verification.")
                    .profileCompleted(false)
                    .firstTimeLogin(true)
                    .nextAction("VERIFY_OTP")
                    .build());
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(LoginResponse.builder().message("Invalid credentials").build());
        }

        // Repair legacy data: if profile exists but flag wasn't set, set it now.
        boolean profileCompleted = user.isProfileCompleted();
        if (!profileCompleted) {
            boolean profileExists = playerProfileRepository.findByUserDetailsId(user.getId()).isPresent();
            if (profileExists) {
                user.setProfileCompleted(true);
                userDetailRepository.save(user);
                profileCompleted = true;
            }
        }

        boolean firstTimeLogin = !profileCompleted;
        String nextAction = profileCompleted ? "NONE" : "COMPLETE_PROFILE";

        String message = profileCompleted
                ? "Login successful."
                : "Login successful. Please continue to profile creation.";

        String sessionKey = sessionService.createSessionKey(user.getId());

        return ResponseEntity.ok(LoginResponse.builder()
                .message(message)
                .sessionKey(sessionKey)
                .idleTimeoutSeconds(idleTimeoutSeconds)
                .userId(user.getId())
                .userName(user.getUserName())
                .profileCompleted(profileCompleted)
                .firstTimeLogin(firstTimeLogin)
                .nextAction(nextAction)
                .build());
    }

    private Optional<com.surohi.backend.cric_scorer.entity.UserDetail> findUserByLoginId(String loginId) {
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

    private static String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
