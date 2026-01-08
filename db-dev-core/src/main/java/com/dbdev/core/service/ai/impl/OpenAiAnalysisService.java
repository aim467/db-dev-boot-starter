package com.dbdev.core.service.ai.impl;

import com.dbdev.core.config.AiProperties;
import com.dbdev.core.model.ColumnMetadata;
import com.dbdev.core.model.IndexMetadata;
import com.dbdev.core.model.TableMetadata;
import com.dbdev.core.service.ai.AiAnalysisResult;
import com.dbdev.core.service.ai.AiAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * OpenAI 分析服务实现
 */
@Slf4j
@Service
public class OpenAiAnalysisService implements AiAnalysisService {

    private final AiProperties aiProperties;
    private final Map<String, String> responseCache = new ConcurrentHashMap<>();

    public OpenAiAnalysisService(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public AiAnalysisResult analyzeSql(String sql, String databaseType) {
        if (!isEnabled()) {
            return AiAnalysisResult.builder()
                    .success(false)
                    .errorMessage("AI 功能未启用，请在配置文件中设置 db.dev.ai.enabled=true")
                    .build();
        }

        try {
            String cacheKey = generateCacheKey("sql", sql, databaseType);
            if (aiProperties.isCacheEnabled() && responseCache.containsKey(cacheKey)) {
                log.debug("从缓存获取 SQL 分析结果");
                return parseAiResponse(responseCache.get(cacheKey), "SQL");
            }

            String prompt = buildSqlAnalysisPrompt(sql, databaseType);
            String response = callAi(prompt);

            if (aiProperties.isCacheEnabled()) {
                responseCache.put(cacheKey, response);
            }

            return parseAiResponse(response, "SQL");

        } catch (Exception e) {
            log.error("SQL AI 分析失败", e);
            return AiAnalysisResult.builder()
                    .success(false)
                    .errorMessage("AI 分析失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public AiAnalysisResult analyzeExplain(Map<String, Object> explainData, String databaseType) {
        if (!isEnabled()) {
            return AiAnalysisResult.builder()
                    .success(false)
                    .errorMessage("AI 功能未启用")
                    .build();
        }

        try {
            String prompt = buildExplainAnalysisPrompt(explainData, databaseType);
            String response = callAi(prompt);
            return parseAiResponse(response, "EXPLAIN");

        } catch (Exception e) {
            log.error("EXPLAIN AI 分析失败", e);
            return AiAnalysisResult.builder()
                    .success(false)
                    .errorMessage("AI 分析失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public AiAnalysisResult analyzeTableStructure(List<TableMetadata> tables, String databaseType) {
        if (!isEnabled()) {
            return AiAnalysisResult.builder()
                    .success(false)
                    .errorMessage("AI 功能未启用")
                    .build();
        }

        try {
            String cacheKey = generateCacheKey("table", tables.size() + "tables", databaseType);
            if (aiProperties.isCacheEnabled() && responseCache.containsKey(cacheKey)) {
                log.debug("从缓存获取表结构分析结果");
                return parseAiResponse(responseCache.get(cacheKey), "表结构");
            }

            String prompt = buildTableAnalysisPrompt(tables, databaseType);
            String response = callAi(prompt);

            if (aiProperties.isCacheEnabled()) {
                responseCache.put(cacheKey, response);
            }

            return parseAiResponse(response, "表结构");

        } catch (Exception e) {
            log.error("表结构 AI 分析失败", e);
            return AiAnalysisResult.builder()
                    .success(false)
                    .errorMessage("AI 分析失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean isEnabled() {
        return aiProperties.isEnabled() && StringUtils.hasText(aiProperties.getApiKey());
    }

    // ==================== 私有方法 ====================

    private String callAi(String prompt) {
        // TODO: 实际调用 OpenAI API
        // 这里先返回模拟响应，用于测试
        log.info("AI Prompt: {}", prompt);
        return generateMockResponse(prompt);
    }

    private String buildSqlAnalysisPrompt(String sql, String databaseType) {
        return String.format("""
            你是一个数据库性能优化专家。请分析以下 SQL 语句：

            数据库类型: %s

            SQL 语句:
            ```sql
            %s
            ```

            请从以下几个方面分析：
            1. SQL 语句的正确性和可读性
            2. 潜在的性能问题
            3. 是否正确使用了索引
            4. 是否有优化的空间
            5. 最佳实践遵循情况

            请用 JSON 格式返回分析结果，包含字段：
            - summary: 简要总结
            - details: 详细分析 (支持 Markdown)
            - issues: 问题列表 [{title, description, severity, location}]
            - suggestions: 优化建议列表 [{title, description, priority, example, expectedImprovement}]
            - riskLevel: 风险等级 (low/medium/high)
            - performanceImpact: 性能影响评估
            """, databaseType, sql);
    }

    private String buildExplainAnalysisPrompt(Map<String, Object> explainData, String databaseType) {
        String dataStr = formatExplainData(explainData);
        return String.format("""
            你是一个数据库性能优化专家。请分析以下 EXPLAIN 执行计划：

            数据库类型: %s

            EXPLAIN 结果:
            %s

            请从以下几个方面分析：
            1. 是否存在全表扫描 (type=ALL)
            2. 索引使用情况
            3. 是否使用了临时表或文件排序
            4. 预估扫描行数是否合理
            5. 优化建议

            请用 JSON 格式返回分析结果。
            """, databaseType, dataStr);
    }

    private String buildTableAnalysisPrompt(List<TableMetadata> tables, String databaseType) {
        String tableInfo = tables.stream()
                .limit(10) // 最多分析10个表
                .map(this::formatTableInfo)
                .collect(Collectors.joining("\n\n"));

        return String.format("""
            你是一个数据库架构专家。请分析以下表结构：

            数据库类型: %s

            表结构信息:
            %s

            请从以下几个方面分析：
            1. 表设计是否合理
            2. 字段类型选择是否合适
            3. 索引设计是否合理
            4. 是否缺少必要的索引
            5. 命名规范遵循情况
            6. 潜在的性能风险

            请用 JSON 格式返回分析结果，包含：
            - summary: 简要总结
            - issues: 问题列表
            - suggestions: 优化建议列表
            - riskLevel: 风险等级
            """, databaseType, tableInfo);
    }

    private String formatTableInfo(TableMetadata table) {
        StringBuilder sb = new StringBuilder();
        sb.append("表名: ").append(table.getTableName()).append("\n");
        sb.append("表类型: ").append(table.getTableType()).append("\n");
        sb.append("注释: ").append(table.getRemarks()).append("\n");

        if (table.getColumns() != null) {
            sb.append("字段:\n");
            for (ColumnMetadata col : table.getColumns()) {
                sb.append(String.format("  - %s %s(%s)%s%s\n",
                        col.getColumnName(),
                        col.getTypeName(),
                        col.getColumnSize(),
                        col.isPrimaryKey() ? " [主键]" : "",
                        col.isAutoIncrement() ? " [自增]" : ""));
            }
        }

        if (table.getIndexes() != null && !table.getIndexes().isEmpty()) {
            sb.append("索引:\n");
            for (IndexMetadata idx : table.getIndexes()) {
                sb.append(String.format("  - %s (%s) %s\n",
                        idx.getIndexName(),
                        idx.getColumnName(),
                        idx.isUnique() ? "[唯一]" : ""));
            }
        }

        return sb.toString();
    }

    private String formatExplainData(Map<String, Object> explainData) {
        return explainData.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    private String generateCacheKey(String type, String content, String dbType) {
        return type + "_" + Integer.toHexString(content.hashCode()) + "_" + dbType;
    }

    private AiAnalysisResult parseAiResponse(String response, String context) {
        // TODO: 解析 AI 返回的 JSON 响应
        // 这里先返回解析后的结果
        log.info("AI Response for {}: {}", context, response);

        // 模拟解析
        return AiAnalysisResult.builder()
                .success(true)
                .summary("AI 分析完成")
                .details(response)
                .riskLevel("low")
                .performanceImpact("正常")
                .issues(Collections.emptyList())
                .suggestions(Collections.emptyList())
                .examples(Collections.emptyList())
                .build();
    }

    private String generateMockResponse(String prompt) {
        // 模拟 AI 响应，用于开发测试
        if (prompt.contains("EXPLAIN")) {
            return """
                {
                    "summary": "执行计划分析完成，发现1个潜在问题",
                    "details": "该查询执行了全表扫描，可能影响性能",
                    "issues": [
                        {
                            "title": "全表扫描",
                            "description": "查询执行了全表扫描 (type=ALL)，对于大表可能导致性能问题",
                            "severity": "high",
                            "location": "SELECT * FROM users WHERE age > 25"
                        }
                    ],
                    "suggestions": [
                        {
                            "title": "添加索引",
                            "description": "建议为 WHERE 条件中的字段创建索引",
                            "priority": "high",
                            "example": "CREATE INDEX idx_users_age ON users(age)",
                            "expectedImprovement": "可将查询性能提升10-100倍"
                        }
                    ],
                    "riskLevel": "medium",
                    "performanceImpact": "扫描行数较多，建议优化"
                }
                """;
        }

        return """
            {
                "summary": "SQL 语句分析完成",
                "details": "SQL 语句结构清晰，建议添加索引优化查询性能",
                "issues": [],
                "suggestions": [
                    {
                        "title": "使用覆盖索引",
                        "description": "如果查询只涉及少量字段，可考虑使用覆盖索引减少回表",
                        "priority": "medium",
                        "example": "CREATE INDEX idx_covering ON table(col1, col2)",
                        "expectedImprovement": "减少 I/O 操作"
                    }
                ],
                "riskLevel": "low",
                "performanceImpact": "正常"
            }
            """;
    }
}