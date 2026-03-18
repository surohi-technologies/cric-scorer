package com.surohi.backend.cric_scorer.service.serviceImpl;

import com.surohi.backend.cric_scorer.entity.*;
import com.surohi.backend.cric_scorer.repository.*;
import com.surohi.backend.cric_scorer.response.MetaOptionResponse;
import com.surohi.backend.cric_scorer.service.MetaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class MetaServiceImpl implements MetaService {

    private final CountryDialCodeRepository countryDialCodeRepository;
    private final ArmRepository armRepository;
    private final BattingStyleRepository battingStyleRepository;
    private final BattingPositionRepository battingPositionRepository;
    private final BowlingStyleRepository bowlingStyleRepository;
    private final BowlingPreferenceRepository bowlingPreferenceRepository;
    private final BowlingTacticalRoleRepository bowlingTacticalRoleRepository;
    private final PlayerRoleRepository playerRoleRepository;
    private final BattingIntentRepository battingIntentRepository;

    public MetaServiceImpl(CountryDialCodeRepository countryDialCodeRepository,
                           ArmRepository armRepository,
                           BattingStyleRepository battingStyleRepository,
                           BattingPositionRepository battingPositionRepository,
                           BowlingStyleRepository bowlingStyleRepository,
                           BowlingPreferenceRepository bowlingPreferenceRepository,
                           BowlingTacticalRoleRepository bowlingTacticalRoleRepository,
                           PlayerRoleRepository playerRoleRepository,
                           BattingIntentRepository battingIntentRepository) {
        this.countryDialCodeRepository = countryDialCodeRepository;
        this.armRepository = armRepository;
        this.battingStyleRepository = battingStyleRepository;
        this.battingPositionRepository = battingPositionRepository;
        this.bowlingStyleRepository = bowlingStyleRepository;
        this.bowlingPreferenceRepository = bowlingPreferenceRepository;
        this.bowlingTacticalRoleRepository = bowlingTacticalRoleRepository;
        this.playerRoleRepository = playerRoleRepository;
        this.battingIntentRepository = battingIntentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> dialCodes() {
        return countryDialCodeRepository.findAll().stream()
                .sorted(Comparator.comparing(CountryDialCode::getCountryName, String.CASE_INSENSITIVE_ORDER))
                .map(d -> MetaOptionResponse.builder()
                        .id(d.getId() == null ? 0 : d.getId())
                        .label(d.getDialCode() + " " + d.getCountryName())
                        .description(d.getIso2())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> arms() {
        return armRepository.findAll().stream()
                .sorted(Comparator.comparing(Arm::getArmType, String.CASE_INSENSITIVE_ORDER))
                .map(a -> MetaOptionResponse.builder()
                        .id(a.getId())
                        .label(a.getArmType())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> battingStyles() {
        return battingStyleRepository.findAll().stream()
                .sorted(Comparator.comparing(BattingStyle::getStyleName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> MetaOptionResponse.builder()
                        .id(s.getId())
                        .label(s.getStyleName())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> battingPositions() {
        return battingPositionRepository.findAll().stream()
                .sorted(Comparator.comparingInt(p -> p.id))
                .map(p -> MetaOptionResponse.builder()
                        .id(p.id)
                        .label(p.Position == null ? ("Position " + p.id) : p.Position)
                        .description(p.Role)
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> bowlingStyles() {
        return bowlingStyleRepository.findAll().stream()
                .sorted(Comparator.comparing(BowlingStyle::getStyleName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> MetaOptionResponse.builder()
                        .id(s.getId())
                        .label(s.getStyleName())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> bowlingPreferences() {
        return bowlingPreferenceRepository.findAll().stream()
                .sorted(Comparator.comparing(BowlingPreference::getPreferenceName, String.CASE_INSENSITIVE_ORDER))
                .map(p -> MetaOptionResponse.builder()
                        .id(p.getId())
                        .label(p.getPreferenceName())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> bowlingTacticalRoles() {
        return bowlingTacticalRoleRepository.findAll().stream()
                .sorted(Comparator.comparing(BowlingTacticalRole::getRoleName, String.CASE_INSENSITIVE_ORDER))
                .map(r -> MetaOptionResponse.builder()
                        .id(r.getId())
                        .label(r.getRoleName())
                        .description(r.getDescription())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> playerRoleTypes() {
        return playerRoleRepository.findAll().stream()
                .sorted(Comparator.comparing(PlayerRole::getRoleName, String.CASE_INSENSITIVE_ORDER))
                .map(r -> MetaOptionResponse.builder()
                        .id(r.getId())
                        .label(r.getRoleName())
                        .description(r.getDescription())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaOptionResponse> battingIntents() {
        return battingIntentRepository.findAll().stream()
                .sorted(Comparator.comparing(BattingIntent::getIntentName, String.CASE_INSENSITIVE_ORDER))
                .map(i -> MetaOptionResponse.builder()
                        .id(i.getId())
                        .label(i.getIntentName())
                        .description(i.getDescription())
                        .build())
                .toList();
    }
}

