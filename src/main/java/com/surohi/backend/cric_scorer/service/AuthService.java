package com.surohi.backend.cric_scorer.service;

import com.surohi.backend.cric_scorer.request.LoginRequest;
import com.surohi.backend.cric_scorer.response.LoginResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<LoginResponse> login(LoginRequest request);
}

