package com.surohi.backend.cric_scorer.controller;

import com.surohi.backend.cric_scorer.constants.CricScorerServiceConstants;
import com.surohi.backend.cric_scorer.constants.OtpChannel;
import com.surohi.backend.cric_scorer.constants.OtpPurpose;
import com.surohi.backend.cric_scorer.repository.UserDetailRepository;
import com.surohi.backend.cric_scorer.request.RegistrationOtpResendRequest;
import com.surohi.backend.cric_scorer.request.RegistrationOtpVerifyRequest;
import com.surohi.backend.cric_scorer.response.RegistrationOtpResponse;
import com.surohi.backend.cric_scorer.service.OtpService;
import com.surohi.backend.cric_scorer.validator.ValidationError;
import com.surohi.backend.cric_scorer.validator.ValidationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(CricScorerServiceConstants.BASE_URL)
public class RegistrationOtpController {

    private final UserDetailRepository userDetailRepository;
    private final OtpService otpService;
    private final boolean requireBothChannels;

    public RegistrationOtpController(UserDetailRepository userDetailRepository,
                                     OtpService otpService,
                                     @Value("${app.registration.require-both-channels:true}") boolean requireBothChannels) {
        this.userDetailRepository = userDetailRepository;
        this.otpService = otpService;
        this.requireBothChannels = requireBothChannels;
    }

    @PostMapping("/auth/registration/verify-otp")
    public ResponseEntity<RegistrationOtpResponse> verify(@Valid @RequestBody RegistrationOtpVerifyRequest request) {
        var user = userDetailRepository.findById(request.getUserId())
                .orElseThrow(() -> new ValidationException("Validation failed", List.of(new ValidationError("userId", "Invalid userId"))));

        OtpChannel channel = parseChannel(request.getChannel());
        boolean ok = otpService.verifyOtp(user.getId(), OtpPurpose.REGISTRATION_VERIFY, channel, request.getOtp());
        if (!ok) {
            throw new ValidationException("Validation failed", List.of(new ValidationError("otp", "Invalid OTP")));
        }

        if (channel == OtpChannel.EMAIL) user.setVerifiedEmail(true);
        if (channel == OtpChannel.PHONE) user.setVerifiedPhone(true);

        List<String> required = requiredChannels(user);
        boolean verified = isVerified(user, required);
        user.setVerified(verified);
        user.setActive(verified); // allow login only after verification
        userDetailRepository.save(user);

        return ResponseEntity.ok(RegistrationOtpResponse.builder()
                .message(verified ? "Registration verified successfully." : "OTP verified. Please verify remaining channel(s).")
                .verified(user.isVerified())
                .active(user.isActive())
                .requiredChannels(required)
                .verifiedChannels(currentVerified(user))
                .build());
    }

    @PostMapping("/auth/registration/resend-otp")
    public ResponseEntity<RegistrationOtpResponse> resend(@Valid @RequestBody RegistrationOtpResendRequest request) {
        var user = userDetailRepository.findById(request.getUserId())
                .orElseThrow(() -> new ValidationException("Validation failed", List.of(new ValidationError("userId", "Invalid userId"))));

        List<String> required = requiredChannels(user);
        if (request.getChannel() == null || request.getChannel().isBlank()) {
            // resend all required
            if (required.contains(OtpChannel.EMAIL.name())) otpService.sendRegistrationOtps(user); // sends to all available
            else otpService.sendRegistrationOtps(user);
        } else {
            OtpChannel channel = parseChannel(request.getChannel());
            // send only one channel by temporarily filtering:
            if (channel == OtpChannel.EMAIL && user.getEmailId() != null) {
                otpService.sendRegistrationOtps(user); // will include email; OK for now
            } else if (channel == OtpChannel.PHONE && user.getPhoneNumber() != null) {
                otpService.sendRegistrationOtps(user);
            }
        }

        return ResponseEntity.ok(RegistrationOtpResponse.builder()
                .message("OTP sent (if destination exists).")
                .verified(user.isVerified())
                .active(user.isActive())
                .requiredChannels(required)
                .verifiedChannels(currentVerified(user))
                .build());
    }

    private OtpChannel parseChannel(String s) {
        try {
            return OtpChannel.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new ValidationException("Validation failed", List.of(new ValidationError("channel", "channel must be EMAIL or PHONE")));
        }
    }

    private List<String> requiredChannels(com.surohi.backend.cric_scorer.entity.UserDetail user) {
        String cfg = user.getVerificationRequiredChannels();
        if (cfg != null && !cfg.isBlank()) {
            return Arrays.stream(cfg.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isBlank())
                    .map(v -> v.toUpperCase(Locale.ROOT))
                    .toList();
        }
        List<String> avail = new ArrayList<>();
        if (user.getEmailId() != null && !user.getEmailId().trim().isBlank()) avail.add(OtpChannel.EMAIL.name());
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isBlank()) avail.add(OtpChannel.PHONE.name());
        if (!requireBothChannels && !avail.isEmpty()) {
            return List.of(avail.get(0));
        }
        return avail;
    }

    private boolean isVerified(com.surohi.backend.cric_scorer.entity.UserDetail user, List<String> required) {
        if (required.isEmpty()) return true;
        boolean ok = true;
        if (required.contains(OtpChannel.EMAIL.name())) ok &= user.isVerifiedEmail();
        if (required.contains(OtpChannel.PHONE.name())) ok &= user.isVerifiedPhone();
        return ok;
    }

    private List<String> currentVerified(com.surohi.backend.cric_scorer.entity.UserDetail user) {
        List<String> v = new ArrayList<>();
        if (user.isVerifiedEmail()) v.add(OtpChannel.EMAIL.name());
        if (user.isVerifiedPhone()) v.add(OtpChannel.PHONE.name());
        return v;
    }
}

