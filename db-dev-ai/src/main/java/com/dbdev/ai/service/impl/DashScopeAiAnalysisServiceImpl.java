package com.dbdev.ai.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.dbdev.ai.config.AiProperties;
import com.dbdev.ai.model.AiAnalysisResult;
import com.dbdev.ai.service.AiAnalysisService;
import com.dbdev.core.model.TableMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * DashScope AI分析服务实现
 */
@Slf4j
public class DashScopeAiAnalysisServiceImpl implements AiAnalysisService {

    private final AiProperties aiProperties;
    private final Generation generation;
    private final ObjectMapper objectMapper;

    public DashScopeAiAnalysisServiceImpl(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.generation = new Generation();
        this.objectMapper = new ObjectMapper();
        
        // 设置API Key
        if (aiProperties.getModelscope().getApiKey() != null) {
            System.setProperty("DASHSCOPE_API_KEY", aiProperties.getModelscope().getApiKey());
        }
    }

    @Override
    public boolean isEnabled() {
        return aiProperties.isEnabled() && 
               aiProperties.getModelscope().getApiKey() != null &&
               !aiProperties.getModelscope().getApiKey().isEmpty();
    }

    @Override
    public AiAnalysisResult analyzeSql(String sql, String databaseType) {
        if (!isEnabled()) {
            return AiAnalysisResult.builder()
                    .success(false)
                    .error("AI功能未启用或未配置API密钥")
                    .build();
        }

        try {
            String prompt = buildSqlAnalysisPrompt(sql, databaseType);
            String response = callModelScope(prompt);

            return AiAnalysisResult.builder()
                    .success(true)
                    .type("sql_analysis")
                    .analysis(response)
                    .rawResponse(response)
                    .build();
        } catch (Exception e) {
            log.error("SQL分析失败", e);
            return AiAnalysisResult.builder()
                    .success(false)
                    .error("分析失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public AiAnalysisResult analyzeExplain(Map<String, Object> explainData, String databaseType) {
        if (!isEnabled()) {
            return AiAnalysisResult.builder()
                    .success(false)
                    .error("AI功能未启用或未配置API密钥")
                    .build();
        }

        try {
            String explainJson = objectMapper.writeValueAsString(explainData);
            String prompt = buildExplainAnalysisPrompt(explainJson, databaseType);
            String response = callModelScope(prompt);

            return AiAnalysisResult.builder()
                    .success(true)
                    .type("explain_analysis")
                    .analysis(response)
                    .rawResponse(response)
                    .build();
        } catch (Exception e) {
            log.error("执行计划分析失败", e);
            return AiAnalysisResult.builder()
                    .success(false)
                    .error("分析失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public AiAnalysisResult analyzeTableStructure(List<TableMetadata> tables, String databaseType) {
        if (!isEnabled()) {
            return AiAnalysisResult.builder()
                    .success(false)
                    .error("AI功能未启用或未配置API密钥")
                    .build();
        }

        try {
            String tablesJson = objectMapper.writeValueAsString(tables);
            String prompt = buildTableAnalysisPrompt(tablesJson, databaseType);
            String response = callModelScope(prompt);

            return AiAnalysisResult.builder()
                    .success(true)
                    .type("table_structure_analysis")
                    .analysis(response)
                    .suggestion(extractSuggestions(response))
                    .rawResponse(response)
                    .build();
        } catch (Exception e) {
            log.error("表结构分析失败", e);
            return AiAnalysisResult.builder()
                    .success(false)
                    .error("分析失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 调用ModelScope API
     */
    private String callModelScope(String prompt) throws NoApiKeyException, ApiException, InputRequiredException {
        Message systemMessage = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个专业的数据库专家，精通SQL优化、执行计划分析和数据库设计。请用中文回答，提供专业、详细且实用的建议。")
                .build();

        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(prompt)
                .build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(aiProperties.getModelscope().getApiKey())
                .model(aiProperties.getModelscope().getModel())
                .messages(Arrays.asList(systemMessage, userMessage))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .temperature((float) aiProperties.getModelscope().getTemperature())
                .maxTokens(aiProperties.getModelscope().getMaxTokens())
                .enableSearch(false)
                .incrementalOutput(false)
                .build();

        GenerationResult result = generation.call(param);
        
        if (result != null && result.getOutput() != null && result.getOutput().getChoices() != null) {
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        }
        
        throw new RuntimeException("API返回结果为空");
    }

    /**
     * 构建SQL分析提示词
     */
    private String buildSqlAnalysisPrompt(String sql, String databaseType) {
        return String.format("""
                请分析以下%s数据库的SQL语句，并提供详细的分析报告：
                
                ```sql
                %s
                ```
                
                请从以下几个方面进行分析：
                1. SQL语句的功能和目的
                2. 可能存在的性能问题
                3. 索引使用建议
                4. 优化建议（如果有）
                5. 潜在的风险或注意事项
                
                请用清晰的结构化格式回答。
                """, databaseType, sql);
    }

    /**
     * 构建执行计划分析提示词
     */
    private String buildExplainAnalysisPrompt(String explainJson, String databaseType) {
        return String.format("""
                请分析以下%s数据库的SQL执行计划：
                
                ```json
                %s
                ```
                
                请提供：
                1. 执行计划的整体评估
                2. 关键性能指标分析（扫描行数、查询成本等）
                3. 是否使用了索引，索引使用是否合理
                4. 潜在的性能瓶颈
                5. 具体的优化建议
                
                请用清晰的结构化格式回答。
                """, databaseType, explainJson);
    }

    /**
     * 构建表结构分析提示词
     */
    private String buildTableAnalysisPrompt(String tablesJson, String databaseType) {
        return String.format("""
                请分析以下%s数据库的表结构设计：
                
                ```json
                %s
                ```
                
                请从以下方面进行分析：
                1. 表结构设计是否合理
                2. 字段类型选择是否恰当
                3. 索引设计建议
                4. 主键和外键关系
                5. 数据库设计规范性评估
                6. 性能优化建议
                
                请用清晰的结构化格式回答。
                """, databaseType, tablesJson);
    }

    /**
     * 从响应中提取建议部分
     */
    private String extractSuggestions(String response) {
        if (response == null) {
            return null;
        }
        
        // 简单提取包含"建议"的部分
        String[] lines = response.split("\n");
        StringBuilder suggestions = new StringBuilder();
        boolean inSuggestionSection = false;
        
        for (String line : lines) {
            if (line.contains("建议") || line.contains("优化") || line.contains("改进")) {
                inSuggestionSection = true;
            }
            if (inSuggestionSection) {
                suggestions.append(line).append("\n");
            }
        }
        
        return suggestions.length() > 0 ? suggestions.toString().trim() : null;
    }
}
