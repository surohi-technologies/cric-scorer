package com.surohi.backend.cric_scorer.entity;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profile", schema = CricScorerServiceConstants.PUBLIC_SCHEMA)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerProfile extends Base{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_details_id", referencedColumnName = "id", nullable = false, unique = true)
    private UserDetail userDetails;

    // Alias Name
    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private int jerseyNumber;

    // Hands
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_hand_id")
    private Arm battingHand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_hand_id")
    private Arm bowlingHand;

    // Styles / Roles / Preferences / Positions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_style_id")
    private BattingStyle battingStyle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_style_id")
    private BowlingStyle bowlingStyle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_tactical_role_id")
    private BowlingTacticalRole bowlingTacticalRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_preference_id")
    private BowlingPreference bowlingPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_position_id")
    private BattingPosition battingPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_role_type_id")
    private PlayerRole playerRoleType;

    // Optional spice: intent (anchoring/attacking/finisher etc.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_intent_id")
    private BattingIntent battingIntent;

    @Column
    private String favouritePlayer;

    @Column
    private String favouriteTeam;

    @Column
    private String description;
}
