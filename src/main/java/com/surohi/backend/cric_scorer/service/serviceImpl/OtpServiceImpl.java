package com.surohi.backend.cric_scorer.service.serviceImpl;

import com.surohi.backend.cric_scorer.constants.OtpChannel;
import com.surohi.backend.cric_scorer.constants.OtpPurpose;
import com.surohi.backend.cric_scorer.entity.UserDetail;
import com.surohi.backend.cric_scorer.entity.UserOtp;
import com.surohi.backend.cric_scorer.repository.UserOtpRepository;
import com.surohi.backend.cric_scorer.service.OtpService;
import com.surohi.backend.cric_scorer.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);
    private final SecureRandom random = new SecureRandom();
    private final UserOtpRepository userOtpRepository;
    private final long otpExpirySeconds;
    private final int maxAttempts;
    private final String pepper;
    private final boolean devLogOtp;
    private final String fixedOtp;

    public OtpServiceImpl(UserOtpRepository userOtpRepository,
                          @Value("${app.otp.expiry-seconds:300}") long otpExpirySeconds,
                          @Value("${app.otp.max-attempts:5}") int maxAttempts,
                          @Value("${app.otp.pepper:cric-scorer}") String pepper,
                          @Value("${app.otp.dev-log:true}") boolean devLogOtp,
                          @Value("${app.otp.fixed-otp:123456}") String fixedOtp) {
        this.userOtpRepository = userOtpRepository;
        this.otpExpirySeconds = Math.max(30, otpExpirySeconds);
        this.maxAttempts = Math.max(1, maxAttempts);
        this.pepper = pepper == null ? "cric-scorer" : pepper;
        this.devLogOtp = devLogOtp;
        this.fixedOtp = normalizeFixedOtp(fixedOtp);
    }

    @Override
    @Transactional
    public List<OtpChannel> sendRegistrationOtps(UserDetail user) {
        List<OtpChannel> channels = new ArrayList<>();
        if (hasEmail(user)) channels.add(OtpChannel.EMAIL);
        if (hasPhone(user)) channels.add(OtpChannel.PHONE);
        for (OtpChannel ch : channels) {
            sendOtpInternal(user, OtpPurpose.REGISTRATION_VERIFY, ch);
        }
        return channels;
    }

    @Override
    @Transactional
    public boolean sendPasswordResetOtp(UserDetail user, OtpChannel channel) {
        if (channel == OtpChannel.EMAIL && !hasEmail(user)) return false;
        if (channel == OtpChannel.PHONE && !hasPhone(user)) return false;
        sendOtpInternal(user, OtpPurpose.PASSWORD_RESET, channel);
        return true;
    }

    @Override
    @Transactional
    public boolean verifyOtp(Long userId, OtpPurpose purpose, OtpChannel channel, String otp) {
        if (otp == null) return false;
        String normalized = otp.trim();
        if (normalized.isEmpty()) return false;

        var opt = userOtpRepository.findLatestActive(userId, purpose.name(), channel.name());
        if (opt.isEmpty()) return false;

        UserOtp row = opt.get();
        Instant now = Instant.now();

        if (row.getExpiresAt() != null && now.isAfter(row.getExpiresAt())) {
            row.setConsumedAt(now);
            userOtpRepository.save(row);
            return false;
        }
        if (row.getAttempts() >= row.getMaxAttempts()) {
            return false;
        }

        boolean ok = CryptoUtil.sha256Hex(normalized + ":" + pepper).equalsIgnoreCase(row.getOtpHash());
        if (ok) {
            row.setConsumedAt(now);
            userOtpRepository.save(row);
            return true;
        }

        row.setAttempts(row.getAttempts() + 1);
        userOtpRepository.save(row);
        return false;
    }

    private void sendOtpInternal(UserDetail user, OtpPurpose purpose, OtpChannel channel) {
        String destination = destination(user, channel);
        if (destination == null) return;

        // Dev/default behavior: fixed OTP so we can test without SMS/email integration.
        // Override by setting app.otp.fixed-otp= (empty) or a different 6-digit value.
        String otp = fixedOtp != null ? fixedOtp : generateOtp6();
        Instant now = Instant.now();

        // Consume any existing active OTPs for this purpose/channel.
        userOtpRepository.consumeAllActive(user.getId(), purpose.name(), channel.name(), now);

        UserOtp row = new UserOtp();
        row.setUser(user);
        row.setPurpose(purpose.name());
        row.setChannel(channel.name());
        row.setDestination(destination);
        row.setCreatedAt(now);
        row.setExpiresAt(now.plusSeconds(otpExpirySeconds));
        row.setAttempts(0);
        row.setMaxAttempts(maxAttempts);
        row.setOtpHash(CryptoUtil.sha256Hex(otp + ":" + pepper));

        userOtpRepository.save(row);

        // Delivery stub: for now just log. Later plug SMS/email providers here.
        if (devLogOtp) {
            log.info("OTP [{}] purpose={} channel={} destination={}", otp, purpose.name(), channel.name(), mask(destination, channel));
        } else {
            log.info("OTP generated purpose={} channel={} destination={}", purpose.name(), channel.name(), mask(destination, channel));
        }
    }

    private String destination(UserDetail user, OtpChannel channel) {
        if (channel == OtpChannel.EMAIL) {
            return normalizeNullable(user.getEmailId());
        }
        // PHONE
        String dial = normalizeNullable(user.getPhoneCountryCode());
        String phone = normalizeNullable(user.getPhoneNumber());
        if (dial == null || phone == null) return null;
        return dial + phone;
    }

    private boolean hasEmail(UserDetail user) {
        String v = normalizeNullable(user.getEmailId());
        return v != null && !v.isBlank();
    }

    private boolean hasPhone(UserDetail user) {
        String p = normalizeNullable(user.getPhoneNumber());
        String d = normalizeNullable(user.getPhoneCountryCode());
        return p != null && !p.isBlank() && d != null && !d.isBlank();
    }

    private String generateOtp6() {
        int n = random.nextInt(900000) + 100000;
        return String.valueOf(n);
    }

    private static String normalizeFixedOtp(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        if (v.length() != 6) return null;
        for (int i = 0; i < v.length(); i++) {
            if (!Character.isDigit(v.charAt(i))) return null;
        }
        return v;
    }

    private static String normalizeNullable(String v) {
        return v == null ? null : v.trim();
    }

    private static String mask(String destination, OtpChannel ch) {
        if (destination == null) return "";
        String s = destination.trim();
        if (s.isEmpty()) return s;
        if (ch == OtpChannel.EMAIL) {
            int at = s.indexOf('@');
            if (at <= 1) return "***" + s.substring(Math.max(0, at));
            return s.substring(0, 1) + "***" + s.substring(at);
        }
        // PHONE
        int keep = Math.min(4, s.length());
        return "***" + s.substring(s.length() - keep);
    }
}
