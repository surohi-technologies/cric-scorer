package com.surohi.backend.cric_scorer.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordStartResponse {
    private String message;
    private List<String> availableChannels; // EMAIL / PHONE (may be empty)
    private String sentChannel;            // EMAIL / PHONE (may be null)
}

