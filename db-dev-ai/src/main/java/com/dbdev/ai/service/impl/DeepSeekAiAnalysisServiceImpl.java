package com.dbdev.ai.service.impl;

import com.dbdev.ai.config.AiProperties;
import com.dbdev.ai.model.AiAnalysisResult;
import com.dbdev.ai.service.AiAnalysisService;
import com.dbdev.core.model.TableMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek AI分析服务实现
 */
@Slf4j
public class DeepSeekAiAnalysisServiceImpl implements AiAnalysisService {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    public DeepSeekAiAnalysisServiceImpl(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return aiProperties.isEnabled() &&
               aiProperties.getProvider().equalsIgnoreCase("deepseek") &&
               StringUtils.hasText(aiProperties.getDeepseek().getApiKey());
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
            String response = callDeepSeek(prompt);

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
            String response = callDeepSeek(prompt);

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
            String response = callDeepSeek(prompt);

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
     * 调用DeepSeek API
     */
    private String callDeepSeek(String prompt) throws IOException {
        AiProperties.DeepSeekConfig config = aiProperties.getDeepseek();

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());

        // 构建消息
        List<Map<String, String>> messages = new ArrayList<>();

        // System message
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的数据库专家，精通SQL优化、执行计划分析和数据库设计。请用中文回答，提供专业、详细且实用的建议。");
        messages.add(systemMessage);

        // User message
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(config.getEndpoint())
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, JSON_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API请求失败: " + response);
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // 检查是否有错误
            if (rootNode.has("error")) {
                throw new IOException("API错误: " + rootNode.get("error").asText());
            }

            // 提取回复内容
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }

            throw new IOException("API返回结果格式异常");
        }
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
