package com.surohi.backend.cric_scorer.service;

import com.surohi.backend.cric_scorer.entity.UserDetail;

public interface PasswordService {
    /**
     * Applies password policy + last-3 history check, then updates user.password and password history.
     */
    void changePassword(UserDetail user, String newPassword);

    /**
     * Records initial password hash into history (used at registration).
     */
    void recordInitialPassword(UserDetail user);
}

