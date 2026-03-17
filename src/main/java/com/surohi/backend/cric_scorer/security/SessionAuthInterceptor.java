package com.surohi.backend.cric_scorer.security;

import com.surohi.backend.cric_scorer.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;
    private final AuthContext authContext;

    public SessionAuthInterceptor(SessionService sessionService, AuthContext authContext) {
        this.sessionService = sessionService;
        this.authContext = authContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.endsWith("/user/register") || path.endsWith("/auth/login")) {
            return true;
        }

        String sessionKey = extractSessionKey(request);
        if (sessionKey == null || sessionKey.isBlank()) {
            response.setStatus(401);
            return false;
        }

        Long userId = sessionService.validateAndTouch(sessionKey);
        if (userId == null) {
            response.setStatus(401);
            return false;
        }
        authContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        authContext.clear();
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