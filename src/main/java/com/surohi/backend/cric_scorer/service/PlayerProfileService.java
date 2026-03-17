package com.surohi.backend.cric_scorer.service;


import com.surohi.backend.cric_scorer.request.PlayerProfileRequest;
import com.surohi.backend.cric_scorer.response.PlayerProfileResponse;
import org.springframework.http.ResponseEntity;

public interface PlayerProfileService {
    ResponseEntity<PlayerProfileResponse> createPlayerProfile(PlayerProfileRequest request);
}
