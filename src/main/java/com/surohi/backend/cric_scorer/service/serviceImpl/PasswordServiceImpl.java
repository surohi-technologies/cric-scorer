package com.surohi.backend.cric_scorer.service.serviceImpl;

import com.surohi.backend.cric_scorer.entity.UserDetail;
import com.surohi.backend.cric_scorer.entity.UserPasswordHistory;
import com.surohi.backend.cric_scorer.repository.UserDetailRepository;
import com.surohi.backend.cric_scorer.repository.UserPasswordHistoryRepository;
import com.surohi.backend.cric_scorer.service.PasswordService;
import com.surohi.backend.cric_scorer.validator.PasswordPolicyValidator;
import com.surohi.backend.cric_scorer.validator.ValidationError;
import com.surohi.backend.cric_scorer.validator.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordServiceImpl implements PasswordService {

    private final UserDetailRepository userDetailRepository;
    private final UserPasswordHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator policyValidator = new PasswordPolicyValidator();

    public PasswordServiceImpl(UserDetailRepository userDetailRepository,
                               UserPasswordHistoryRepository historyRepository,
                               PasswordEncoder passwordEncoder) {
        this.userDetailRepository = userDetailRepository;
        this.historyRepository = historyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void changePassword(UserDetail user, String newPassword) {
        List<ValidationError> errors = new ArrayList<>();
        policyValidator.validate("newPassword", newPassword, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        List<UserPasswordHistory> last3 = historyRepository.findTop3ByUser_IdOrderByCreatedAtDesc(user.getId());
        for (UserPasswordHistory h : last3) {
            if (passwordEncoder.matches(newPassword, h.getPasswordHash())) {
                errors.add(new ValidationError("newPassword", "New password must not match your last 3 passwords"));
                throw new ValidationException("Validation failed", errors);
            }
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userDetailRepository.save(user);

        UserPasswordHistory row = new UserPasswordHistory();
        row.setUser(user);
        row.setPasswordHash(user.getPassword());
        row.setCreatedAt(Instant.now());
        historyRepository.save(row);

        trimToLast3(user.getId());
    }

    @Override
    @Transactional
    public void recordInitialPassword(UserDetail user) {
        // record existing user.password (already encoded at registration)
        UserPasswordHistory row = new UserPasswordHistory();
        row.setUser(user);
        row.setPasswordHash(user.getPassword());
        row.setCreatedAt(Instant.now());
        historyRepository.save(row);
        trimToLast3(user.getId());
    }

    private void trimToLast3(Long userId) {
        List<UserPasswordHistory> last3 = historyRepository.findTop3ByUser_IdOrderByCreatedAtDesc(userId);
        if (last3.isEmpty()) return;
        List<Long> keep = last3.stream().map(UserPasswordHistory::getId).toList();
        historyRepository.deleteAllExcept(userId, keep);
    }
}
