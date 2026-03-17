package com.surohi.backend.cric_scorer.controller;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import com.surohi.backend.cric_scorer.request.UserRegistrationRequest;
import com.surohi.backend.cric_scorer.response.UserRegistrationResponse;
import com.surohi.backend.cric_scorer.service.UserDetailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CricScorerServiceConstants.BASE_URL)
public class UserDetailController {

    private final UserDetailService userDetailService;

    public UserDetailController(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @PostMapping(CricScorerServiceConstants.USER_REGISTRATION_URL)
    public ResponseEntity<UserRegistrationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        return userDetailService.register(request);
    }
}

