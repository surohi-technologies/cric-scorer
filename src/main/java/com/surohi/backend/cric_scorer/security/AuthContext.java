package com.surohi.backend.cric_scorer.security;

import org.springframework.stereotype.Component;

@Component
public class AuthContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public Long getUserId() {
        return USER_ID.get();
    }

    public void clear() {
        USER_ID.remove();
    }
}

