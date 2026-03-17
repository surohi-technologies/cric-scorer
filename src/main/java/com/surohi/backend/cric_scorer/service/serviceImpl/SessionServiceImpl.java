package com.surohi.backend.cric_scorer.service.serviceImpl;

import com.surohi.backend.cric_scorer.entity.UserSession;
import com.surohi.backend.cric_scorer.repository.UserDetailRepository;
import com.surohi.backend.cric_scorer.repository.UserSessionRepository;
import com.surohi.backend.cric_scorer.service.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository userSessionRepository;
    private final UserDetailRepository userDetailRepository;
    private final Duration idleTimeout;

    public SessionServiceImpl(UserSessionRepository userSessionRepository,
                              UserDetailRepository userDetailRepository,
                              @Value("${app.session.idle-timeout-seconds:60}") long idleTimeoutSeconds) {
        this.userSessionRepository = userSessionRepository;
        this.userDetailRepository = userDetailRepository;
        this.idleTimeout = Duration.ofSeconds(Math.max(1, idleTimeoutSeconds));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String createSessionKey(Long userId) {
        var user = userDetailRepository.findById(userId).orElseThrow();
        String sessionKey = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionKeyHash(sha256Hex(sessionKey));
        session.setCreatedAt(Instant.now());
        session.setLastActivityAt(Instant.now());
        session.setActive(true);
        userSessionRepository.save(session);

        // Return raw key once; store only the hash in DB.
        return sessionKey;
    }

    @Override
    @Transactional
    public Long validateAndTouch(String sessionKey) {
        String hash = sha256Hex(sessionKey);
        var sessionOpt = userSessionRepository.findBySessionKeyHashAndIsActiveTrue(hash);
        if (sessionOpt.isEmpty()) {
            return null;
        }
        UserSession session = sessionOpt.get();
        Instant now = Instant.now();
        if (Duration.between(session.getLastActivityAt(), now).compareTo(idleTimeout) > 0) {
            session.setActive(false);
            userSessionRepository.save(session);
            return null;
        }
        session.setLastActivityAt(now);
        userSessionRepository.save(session);
        return session.getUser().getId();
    }

    @Override
    @Transactional
    public void logout(String sessionKey) {
        String hash = sha256Hex(sessionKey);
        var sessionOpt = userSessionRepository.findBySessionKeyHashAndIsActiveTrue(hash);
        sessionOpt.ifPresent(s -> {
            s.setActive(false);
            userSessionRepository.save(s);
        });
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
