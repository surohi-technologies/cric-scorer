package com.surohi.backend.cric_scorer.service;


import com.surohi.backend.cric_scorer.request.UserRegistrationRequest;
import com.surohi.backend.cric_scorer.response.UserRegistrationResponse;
import org.springframework.http.ResponseEntity;

public interface UserDetailService {
    ResponseEntity<UserRegistrationResponse> register(UserRegistrationRequest request);
}

