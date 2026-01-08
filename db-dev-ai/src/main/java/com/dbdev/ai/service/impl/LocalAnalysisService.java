package com.dbdev.ai.service.impl;

import com.dbdev.ai.config.AiProperties;
import com.dbdev.ai.model.AiAnalysisResult;
import com.dbdev.ai.service.AiAnalysisService;
import com.dbdev.core.model.TableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LocalAnalysisService implements AiAnalysisService {

    private final AiProperties aiProperties;

    public LocalAnalysisService(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public boolean isEnabled() {
        return aiProperties.isEnabled();
    }

    @Override
    public AiAnalysisResult analyzeSql(String sql, String databaseType) {
        if (!isEnabled()) {
            return AiAnalysisResult.builder()
                    .success(false)
                    .error("Local AI service is not enabled")
                    .build();
        }

        try {
            return AiAnalysisResult.builder()
                    .success(true)
                    .type("SQL")
                    .analysis("Basic SQL analysis from local service")
                    .suggestion("Consider using OpenAI for more detailed analysis")
                    .build();
        } catch (Exception e) {
            log.error("Local SQL analysis failed", e);
            return AiAnalysisResult.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    @Override
    public AiAnalysisResult analyzeExplain(Map<String, Object> explainData, String databaseType) {
        // Similar implementation
        return null;
    }

    @Override
    public AiAnalysisResult analyzeTableStructure(List<TableMetadata> tables, String databaseType) {
        // Similar implementation
        return null;
    }
}