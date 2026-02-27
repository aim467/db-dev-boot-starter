package com.dbdev.ai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiAnalysisResult {
    private boolean success;
    private String type;
    private String analysis;
    private String suggestion;
    private String rawResponse;
    private String error;
}