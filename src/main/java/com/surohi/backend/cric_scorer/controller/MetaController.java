package com.surohi.backend.cric_scorer.controller;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import com.surohi.backend.cric_scorer.response.MetaOptionResponse;
import com.surohi.backend.cric_scorer.service.MetaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(CricScorerServiceConstants.BASE_URL + "/meta")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    @GetMapping("/dial-codes")
    public List<MetaOptionResponse> dialCodes() {
        return metaService.dialCodes();
    }

    @GetMapping("/arms")
    public List<MetaOptionResponse> arms() {
        return metaService.arms();
    }

    @GetMapping("/batting-styles")
    public List<MetaOptionResponse> battingStyles() {
        return metaService.battingStyles();
    }

    @GetMapping("/batting-positions")
    public List<MetaOptionResponse> battingPositions() {
        return metaService.battingPositions();
    }

    @GetMapping("/bowling-styles")
    public List<MetaOptionResponse> bowlingStyles() {
        return metaService.bowlingStyles();
    }

    @GetMapping("/bowling-preferences")
    public List<MetaOptionResponse> bowlingPreferences() {
        return metaService.bowlingPreferences();
    }

    @GetMapping("/bowling-tactical-roles")
    public List<MetaOptionResponse> bowlingTacticalRoles() {
        return metaService.bowlingTacticalRoles();
    }

    @GetMapping("/player-role-types")
    public List<MetaOptionResponse> playerRoleTypes() {
        return metaService.playerRoleTypes();
    }

    @GetMapping("/batting-intents")
    public List<MetaOptionResponse> battingIntents() {
        return metaService.battingIntents();
    }
}

