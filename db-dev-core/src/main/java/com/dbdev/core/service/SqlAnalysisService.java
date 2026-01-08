package com.dbdev.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * SQL分析服务 - 提供SQL执行计划和性能分析
 */
@Slf4j
@Service
public class SqlAnalysisService {

    private final DataSourceService dataSourceService;

    public SqlAnalysisService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    /**
     * 执行SQL分析
     *
     * @param dataSourceName 数据源名称
     * @param sql SQL查询语句
     * @return 分析结果
     */
    public AnalysisResult analyzeQuery(String dataSourceName, String sql) {
        // 验证SQL语句
        SqlValidationResult validation = validateSql(sql);
        if (!validation.isValid()) {
            return AnalysisResult.error(validation.getErrorMessage());
        }

        DataSource dataSource;
        try {
            dataSource = dataSourceService.getDataSource(dataSourceName);
        } catch (Exception e) {
            log.error("Failed to get data source: {}", dataSourceName, e);
            return AnalysisResult.error("数据源不存在: " + dataSourceName);
        }

        String databaseType = getDatabaseType(dataSource);
        return executeExplain(dataSource, sql, databaseType);
    }

    /**
     * 获取数据库类型
     */
    private String getDatabaseType(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String productName = metaData.getDatabaseProductName().toLowerCase();
            if (productName.contains("mysql")) {
                return "mysql";
            } else if (productName.contains("postgresql")) {
                return "postgresql";
            } else if (productName.contains("oracle")) {
                return "oracle";
            } else if (productName.contains("sql server")) {
                return "sqlserver";
            }
            return "unknown";
        } catch (SQLException e) {
            log.warn("Failed to detect database type", e);
            return "unknown";
        }
    }

    /**
     * 执行EXPLAIN分析
     */
    private AnalysisResult executeExplain(DataSource dataSource, String sql, String databaseType) {
        String explainSql = buildExplainSql(sql, databaseType);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(explainSql);
             ResultSet rs = stmt.executeQuery()) {

            List<Map<String, Object>> explainResults = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                explainResults.add(row);
            }

            // 生成性能优化建议
            List<OptimizationSuggestion> suggestions = analyzeAndGenerateSuggestions(explainResults, databaseType);

            return AnalysisResult.success(explainResults, suggestions, databaseType);

        } catch (SQLException e) {
            log.error("SQL explain error", e);
            return AnalysisResult.error("EXPLAIN执行失败: " + e.getMessage());
        }
    }

    /**
     * 构建EXPLAIN SQL语句
     */
    private String buildExplainSql(String sql, String databaseType) {
        switch (databaseType) {
            case "mysql":
                // MySQL支持多种EXPLAIN格式
                return "EXPLAIN " + sql;
            case "postgresql":
                // PostgreSQL使用EXPLAIN (ANALYZE, FORMAT JSON)获取更详细信息
                return "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) " + sql;
            case "oracle":
                // Oracle使用EXPLAIN PLAN
                return "EXPLAIN PLAN FOR " + sql;
            case "sqlserver":
                // SQL Server使用SET SHOWPLAN_TEXT ON
                return "SET SHOWPLAN_TEXT ON; GO; " + sql + "; GO; SET SHOWPLAN_TEXT OFF;";
            default:
                return "EXPLAIN " + sql;
        }
    }

    /**
     * 分析执行计划并生成优化建议
     */
    private List<OptimizationSuggestion> analyzeAndGenerateSuggestions(
            List<Map<String, Object>> explainResults, String databaseType) {

        List<OptimizationSuggestion> suggestions = new ArrayList<>();

        if (explainResults.isEmpty()) {
            return suggestions;
        }

        // 检测全表扫描
        boolean hasFullTableScan = false;
        boolean hasUsingIndex = false;
        Double totalRows = 0.0;
        Double totalCost = 0.0;

        for (Map<String, Object> row : explainResults) {
            // 检测MySQL执行计划
            if (row.containsKey("type")) {
                String type = String.valueOf(row.get("type"));
                if ("ALL".equals(type)) {
                    hasFullTableScan = true;
                } else if ("index".equals(type) || "const".equals(type) || "eq_ref".equals(type)) {
                    hasUsingIndex = true;
                }
            }

            // 检测PostgreSQL执行计划
            if (row.containsKey("Node Type")) {
                String nodeType = String.valueOf(row.get("Node Type"));
                if ("Seq Scan".equals(nodeType)) {
                    hasFullTableScan = true;
                }
            }

            // 累加行数和成本
            if (row.containsKey("rows")) {
                try {
                    totalRows += Double.parseDouble(String.valueOf(row.get("rows")));
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
            if (row.containsKey("Cost")) {
                try {
                    String costStr = String.valueOf(row.get("Cost"));
                    if (costStr.contains("..")) {
                        costStr = costStr.split("\\.\\.")[1];
                    }
                    totalCost += Double.parseDouble(costStr);
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        }

        // 生成建议
        if (hasFullTableScan) {
            suggestions.add(new OptimizationSuggestion(
                    "high",
                    "检测到全表扫描",
                    "建议为查询条件中的字段添加索引，避免全表扫描以提升查询性能。",
                    "CREATE INDEX idx_table_column ON table_name(column_name)"
            ));
        }

        if (!hasUsingIndex && !hasFullTableScan) {
            suggestions.add(new OptimizationSuggestion(
                    "medium",
                    "未使用索引",
                    "请检查查询条件字段是否已创建索引，可使用EXPLAIN查看索引使用情况。",
                    "使用EXPLAIN ANALYZE查看详细的执行计划"
            ));
        }

        if (totalRows != null && totalRows > 10000) {
            suggestions.add(new OptimizationSuggestion(
                    "medium",
                    "大结果集查询",
                    "查询涉及 " + String.format("%.0f", totalRows) + " 行数据，建议：\n" +
                            "1. 添加合适的索引\n" +
                            "2. 使用分页查询限制返回行数\n" +
                            "3. 考虑使用覆盖索引",
                    "SELECT * FROM table WHERE condition LIMIT 100"
            ));
        }

        if (totalCost != null && totalCost > 1000) {
            suggestions.add(new OptimizationSuggestion(
                    "low",
                    "高成本查询",
                    "查询成本为 " + String.format("%.2f", totalCost) + "，建议优化查询逻辑或添加索引。",
                    null
            ));
        }

        return suggestions;
    }

    /**
     * 验证SQL语句
     */
    private SqlValidationResult validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return SqlValidationResult.invalid("SQL语句不能为空");
        }

        String trimmedSql = sql.trim().toLowerCase(Locale.ROOT);

        // 检查是否以注释开头
        if (trimmedSql.startsWith("--") || trimmedSql.startsWith("/*")) {
            return SqlValidationResult.invalid("不支持纯注释语句");
        }

        // 移除注释
        String cleanSql = trimmedSql.replaceAll("--.*$", "")
                .replaceAll("/\\*.*?\\*/", "")
                .trim();

        // 检查是否为空
        if (cleanSql.isEmpty()) {
            return SqlValidationResult.invalid("SQL语句不能为空");
        }

        // 检查是否为SELECT语句
        if (!cleanSql.startsWith("select")) {
            return SqlValidationResult.invalid("只允许执行SELECT查询语句的分析");
        }

        // 检查是否包含危险关键词
        String[] dangerousKeywords = {
                "insert", "update", "delete", "drop", "create", "alter", "truncate",
                "grant", "revoke", "exec", "execute", "sp_", "xp_"
        };

        for (String keyword : dangerousKeywords) {
            if (cleanSql.contains(keyword)) {
                return SqlValidationResult.invalid("SQL语句包含危险操作: " + keyword);
            }
        }

        return SqlValidationResult.valid();
    }

    /**
     * SQL验证结果
     */
    private static class SqlValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private SqlValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static SqlValidationResult valid() {
            return new SqlValidationResult(true, null);
        }

        public static SqlValidationResult invalid(String errorMessage) {
            return new SqlValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 分析结果
     */
    public static class AnalysisResult {
        private boolean success;
        private String message;
        private List<Map<String, Object>> explainData;
        private List<OptimizationSuggestion> suggestions;
        private String databaseType;

        public static AnalysisResult success(List<Map<String, Object>> explainData,
                                             List<OptimizationSuggestion> suggestions,
                                             String databaseType) {
            AnalysisResult result = new AnalysisResult();
            result.success = true;
            result.message = "分析完成";
            result.explainData = explainData;
            result.suggestions = suggestions;
            result.databaseType = databaseType;
            return result;
        }

        public static AnalysisResult error(String message) {
            AnalysisResult result = new AnalysisResult();
            result.success = false;
            result.message = message;
            return result;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<Map<String, Object>> getExplainData() { return explainData; }
        public List<OptimizationSuggestion> getSuggestions() { return suggestions; }
        public String getDatabaseType() { return databaseType; }
    }

    /**
     * 优化建议
     */
    public static class OptimizationSuggestion {
        private String priority; // high, medium, low
        private String title;
        private String description;
        private String example;

        public OptimizationSuggestion(String priority, String title, String description, String example) {
            this.priority = priority;
            this.title = title;
            this.description = description;
            this.example = example;
        }

        // Getters
        public String getPriority() { return priority; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getExample() { return example; }
    }
}