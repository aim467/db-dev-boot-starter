package com.dbdev.ai.service;

import com.dbdev.ai.model.AiAnalysisResult;
import com.dbdev.core.model.TableMetadata;

import java.util.List;
import java.util.Map;

public interface AiAnalysisService {

    boolean isEnabled();

    AiAnalysisResult analyzeSql(String sql, String databaseType);

    AiAnalysisResult analyzeExplain(Map<String, Object> explainData, String databaseType);

    AiAnalysisResult analyzeTableStructure(List<TableMetadata> tables, String databaseType);
}