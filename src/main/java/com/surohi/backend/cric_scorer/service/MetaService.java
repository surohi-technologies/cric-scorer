package com.surohi.backend.cric_scorer.service;

import com.surohi.backend.cric_scorer.response.MetaOptionResponse;

import java.util.List;

public interface MetaService {
    List<MetaOptionResponse> dialCodes();
    List<MetaOptionResponse> arms();
    List<MetaOptionResponse> battingStyles();
    List<MetaOptionResponse> battingPositions();
    List<MetaOptionResponse> bowlingStyles();
    List<MetaOptionResponse> bowlingPreferences();
    List<MetaOptionResponse> bowlingTacticalRoles();
    List<MetaOptionResponse> playerRoleTypes();
    List<MetaOptionResponse> battingIntents();
}

