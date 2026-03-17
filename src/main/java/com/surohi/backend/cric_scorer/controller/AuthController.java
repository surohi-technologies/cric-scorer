package com.surohi.backend.cric_scorer.controller;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import com.surohi.backend.cric_scorer.request.LoginRequest;
import com.surohi.backend.cric_scorer.response.LoginResponse;
import com.surohi.backend.cric_scorer.response.LogoutResponse;
import com.surohi.backend.cric_scorer.service.AuthService;
import com.surohi.backend.cric_scorer.service.SessionService;
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

    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
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
