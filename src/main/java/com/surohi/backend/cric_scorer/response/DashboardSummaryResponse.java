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
public class DashboardSummaryResponse {
    private String welcomeName;
    private String userName;

    private String aliasName;
    private Integer jerseyNumber;

    // Example: "Right-hand bat | Right-arm fast | Finisher"
    private String roleLine;

    // Team (future)
    private String teamName;
    private String teamLogoUrl;

    // Indicators
    private int performanceLevel; // 0..100
    private int formPercent;       // 0..100
    private List<String> last5FormColors; // GREEN/YELLOW/RED/GRAY etc
}