package com.surohi.backend.cric_scorer.controller;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import com.surohi.backend.cric_scorer.request.PlayerProfileRequest;
import com.surohi.backend.cric_scorer.response.PlayerProfileResponse;
import com.surohi.backend.cric_scorer.service.PlayerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CricScorerServiceConstants.BASE_URL)
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;

    public PlayerProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @PostMapping(CricScorerServiceConstants.PROFILE_CREATION_URL)
    public ResponseEntity<PlayerProfileResponse> profileCreation
            (@Valid @RequestBody PlayerProfileRequest request){
        return playerProfileService.createPlayerProfile(request);
    }
}
