package com.surohi.backend.cric_scorer.service.serviceImpl;

import com.surohi.backend.cric_scorer.entity.*;
import com.surohi.backend.cric_scorer.repository.*;
import com.surohi.backend.cric_scorer.security.AuthContext;
import com.surohi.backend.cric_scorer.request.PlayerProfileRequest;
import com.surohi.backend.cric_scorer.response.PlayerProfileResponse;
import com.surohi.backend.cric_scorer.service.PlayerProfileService;
import com.surohi.backend.cric_scorer.validator.PlayerProfileValidator;
import com.surohi.backend.cric_scorer.validator.ValidationError;
import com.surohi.backend.cric_scorer.validator.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerProfileServiceImpl implements PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final UserDetailRepository userDetailRepository;
    private final AuthContext authContext;
    private final ArmRepository armRepository;
    private final BattingStyleRepository battingStyleRepository;
    private final BattingPositionRepository battingPositionRepository;
    private final BowlingStyleRepository bowlingStyleRepository;
    private final BowlingTacticalRoleRepository bowlingTacticalRoleRepository;
    private final BowlingPreferenceRepository bowlingPreferenceRepository;
    private final PlayerRoleRepository playerRoleRepository;
    private final BattingIntentRepository battingIntentRepository;
    private final PlayerProfileValidator playerProfileValidator = new PlayerProfileValidator();

    public PlayerProfileServiceImpl(PlayerProfileRepository playerProfileRepository,
                                    UserDetailRepository userDetailRepository,
                                    AuthContext authContext,
                                    ArmRepository armRepository,
                                    BattingStyleRepository battingStyleRepository,
                                    BattingPositionRepository battingPositionRepository,
                                    BowlingStyleRepository bowlingStyleRepository,
                                    BowlingTacticalRoleRepository bowlingTacticalRoleRepository,
                                    BowlingPreferenceRepository bowlingPreferenceRepository,
                                    PlayerRoleRepository playerRoleRepository,
                                    BattingIntentRepository battingIntentRepository) {
        this.playerProfileRepository = playerProfileRepository;
        this.userDetailRepository = userDetailRepository;
        this.authContext = authContext;
        this.armRepository = armRepository;
        this.battingStyleRepository = battingStyleRepository;
        this.battingPositionRepository = battingPositionRepository;
        this.bowlingStyleRepository = bowlingStyleRepository;
        this.bowlingTacticalRoleRepository = bowlingTacticalRoleRepository;
        this.bowlingPreferenceRepository = bowlingPreferenceRepository;
        this.playerRoleRepository = playerRoleRepository;
        this.battingIntentRepository = battingIntentRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<PlayerProfileResponse> createPlayerProfile(PlayerProfileRequest request) {
        playerProfileValidator.validate(request);

        String aliasName = normalizeNullable(request.getAliasName());

        Long currentUserId = authContext.getUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401)
                    .body(PlayerProfileResponse.builder().message("Unauthorized").build());
        }

        var userOpt = userDetailRepository.findById(currentUserId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(PlayerProfileResponse.builder().message("User not found").build());
        }
        var user = userOpt.get();

        // Strong check: if a profile row already exists, treat the profile as completed.
        var existingProfile = playerProfileRepository.findByUserDetailsId(currentUserId);
        if (existingProfile.isPresent()) {
            if (!user.isProfileCompleted()) {
                user.setProfileCompleted(true);
                userDetailRepository.save(user);
            }
            return ResponseEntity.status(409)
                    .body(PlayerProfileResponse.builder().message("Player profile already exists for this user").build());
        }

        if (playerProfileRepository.existsByNickname(aliasName)) {
            return ResponseEntity.status(409)
                    .body(PlayerProfileResponse.builder().message("Nickname already exists").build());
        }

        if (playerProfileRepository.existsByJerseyNumber(request.getJerseyNumber())) {
            return ResponseEntity.status(409)
                    .body(PlayerProfileResponse.builder().message("Jersey number already exists").build());
        }

        // Resolve master-data references
        Arm battingHand = require(armRepository.findById(request.getBattingHandId()), "battingHandId", "Invalid battingHandId");
        Arm bowlingHand = require(armRepository.findById(request.getBowlingHandId()), "bowlingHandId", "Invalid bowlingHandId");
        BattingStyle battingStyle = require(battingStyleRepository.findById(request.getBattingStyleId()), "battingStyleId", "Invalid battingStyleId");
        BattingPosition battingPosition = require(battingPositionRepository.findById(request.getBattingPositionId()), "battingPositionId", "Invalid battingPositionId");
        BowlingStyle bowlingStyle = require(bowlingStyleRepository.findById(request.getBowlingStyleId()), "bowlingStyleId", "Invalid bowlingStyleId");
        BowlingTacticalRole bowlingTacticalRole = require(bowlingTacticalRoleRepository.findById(request.getBowlingTacticalRoleId()), "bowlingTacticalRoleId", "Invalid bowlingTacticalRoleId");
        BowlingPreference bowlingPreference = require(bowlingPreferenceRepository.findById(request.getBowlingPreferenceId()), "bowlingPreferenceId", "Invalid bowlingPreferenceId");
        PlayerRole playerRoleType = require(playerRoleRepository.findById(request.getPlayerRoleTypeId()), "playerRoleTypeId", "Invalid playerRoleTypeId");

        BattingIntent battingIntent = null;
        if (request.getBattingIntentId() != null) {
            battingIntent = require(battingIntentRepository.findById(request.getBattingIntentId()), "battingIntentId", "Invalid battingIntentId");
        }

        PlayerProfile playerProfile = new PlayerProfile();
        playerProfile.setUserDetails(user);
        playerProfile.setNickname(aliasName);
        playerProfile.setJerseyNumber(request.getJerseyNumber());
        playerProfile.setBattingHand(battingHand);
        playerProfile.setBowlingHand(bowlingHand);
        playerProfile.setBattingStyle(battingStyle);
        playerProfile.setBattingPosition(battingPosition);
        playerProfile.setBowlingStyle(bowlingStyle);
        playerProfile.setBowlingTacticalRole(bowlingTacticalRole);
        playerProfile.setBowlingPreference(bowlingPreference);
        playerProfile.setPlayerRoleType(playerRoleType);
        playerProfile.setBattingIntent(battingIntent);
        playerProfile.setFavouritePlayer(normalizeNullable(request.getFavouritePlayer()));
        playerProfile.setFavouriteTeam(normalizeNullable(request.getFavouriteTeam()));
        playerProfile.setDescription(normalizeNullable(request.getDescription()));

        PlayerProfile savedProfile;
        try {
            savedProfile = playerProfileRepository.save(playerProfile);
            user.setProfileCompleted(true);
            userDetailRepository.save(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(PlayerProfileResponse.builder().message("Failed to save player profile").build());
        }

        String birthYear = user.getDob() != null ? String.valueOf(user.getDob().getYear()) : "a glorious year";
        String favPlayer = (savedProfile.getFavouritePlayer() != null && !savedProfile.getFavouritePlayer().isBlank())
                ? savedProfile.getFavouritePlayer()
                : "Cricket Legends";

        String roleName = savedProfile.getPlayerRoleType() != null ? savedProfile.getPlayerRoleType().getRoleName() : "Player";
        String graphicalMessage = String.format(
                "Welcome to the pitch, %s! Born in %s, %s mode ON. Inspired by %s. Your journey starts now!",
                savedProfile.getNickname(),
                birthYear,
                roleName,
                favPlayer
        );

        return ResponseEntity.ok(PlayerProfileResponse.builder()
                .message("Player profile created successfully")
                .profileId(savedProfile.getId())
                .userId(user.getId())
                .aliasName(savedProfile.getNickname())
                .jerseyNumber(savedProfile.getJerseyNumber())
                .graphicalMessage(graphicalMessage)
                .build());
    }

    private static String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }

    private static <T> T require(java.util.Optional<T> value, String field, String message) {
        if (value.isPresent()) {
            return value.get();
        }
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(field, message));
        throw new ValidationException("Validation failed", errors);
    }
}