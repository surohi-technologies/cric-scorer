package com.surohi.backend.cric_scorer.service;

public interface SessionService {
    String createSessionKey(Long userId);
    Long validateAndTouch(String sessionKey);
    void logout(String sessionKey);
}

