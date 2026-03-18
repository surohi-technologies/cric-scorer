package com.surohi.backend.cric_scorer.controller;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import com.surohi.backend.cric_scorer.repository.PlayerProfileRepository;
import com.surohi.backend.cric_scorer.repository.UserDetailRepository;
import com.surohi.backend.cric_scorer.response.DashboardSummaryResponse;
import com.surohi.backend.cric_scorer.security.AuthContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(CricScorerServiceConstants.BASE_URL + "/dashboard")
public class DashboardController {

    private final AuthContext authContext;
    private final UserDetailRepository userDetailRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public DashboardController(AuthContext authContext,
                               UserDetailRepository userDetailRepository,
                               PlayerProfileRepository playerProfileRepository) {
        this.authContext = authContext;
        this.userDetailRepository = userDetailRepository;
        this.playerProfileRepository = playerProfileRepository;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        Long userId = authContext.getUserId();
        if (userId == null) {
            // Interceptor should prevent this, but keep it safe.
            return DashboardSummaryResponse.builder()
                    .welcomeName("Player")
                    .performanceLevel(0)
                    .formPercent(0)
                    .last5FormColors(List.of("GRAY", "GRAY", "GRAY", "GRAY", "GRAY"))
                    .build();
        }

        var user = userDetailRepository.findById(userId).orElse(null);
        var profileOpt = playerProfileRepository.findByUserDetailsId(userId);

        String welcomeName = user != null && user.getFirstName() != null ? user.getFirstName() : "Player";
        String userName = user != null ? user.getUserName() : null;

        String aliasName = null;
        Integer jerseyNumber = null;
        String roleLine = null;

        if (profileOpt.isPresent()) {
            var p = profileOpt.get();
            aliasName = p.getNickname();
            jerseyNumber = p.getJerseyNumber();

            String batting = p.getBattingStyle() != null ? p.getBattingStyle().getStyleName() : null;
            String bowling = null;
            if (p.getBowlingHand() != null && p.getBowlingStyle() != null) {
                bowling = p.getBowlingHand().getArmType() + "-arm " + p.getBowlingStyle().getStyleName();
            } else if (p.getBowlingStyle() != null) {
                bowling = p.getBowlingStyle().getStyleName();
            }

            String intent = p.getBattingIntent() != null ? p.getBattingIntent().getIntentName() : null;
            String role = p.getPlayerRoleType() != null ? p.getPlayerRoleType().getRoleName() : null;

            String part1 = batting != null ? batting : (p.getBattingHand() != null ? p.getBattingHand().getArmType() + "-hand batter" : "Batter");
            String part2 = bowling != null ? (bowling + " bowler") : "Bowler";
            String part3 = intent != null ? intent : (role != null ? role : "Player");
            roleLine = part1 + " | " + part2 + " | " + part3;
        }

        // Until matches exist, keep a reasonable default indicator.
        int performanceLevel = profileOpt.isPresent() ? 62 : 0;
        int formPercent = profileOpt.isPresent() ? 55 : 0;

        return DashboardSummaryResponse.builder()
                .welcomeName(welcomeName)
                .userName(userName)
                .aliasName(aliasName)
                .jerseyNumber(jerseyNumber)
                .roleLine(roleLine)
                .teamName(null)
                .teamLogoUrl(null)
                .performanceLevel(performanceLevel)
                .formPercent(formPercent)
                .last5FormColors(List.of("GRAY", "GRAY", "GRAY", "GRAY", "GRAY"))
                .build();
    }
}