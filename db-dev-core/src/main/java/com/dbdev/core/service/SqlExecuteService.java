package com.dbdev.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * SQL执行服务 - 仅支持查询语句
 */
@Slf4j
@Service
public class SqlExecuteService {

    private final DataSourceService dataSourceService;

    public SqlExecuteService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    /**
     * 执行SQL查询
     *
     * @param dataSourceName 数据源名称
     * @param sql SQL查询语句
     * @param params 查询参数
     * @return 查询结果
     */
    public SqlQueryResult executeQuery(String dataSourceName, String sql, List<Object> params) {
        // 验证SQL语句
        SqlValidationResult validation = validateSql(sql);
        if (!validation.isValid()) {
            return SqlQueryResult.error(validation.getErrorMessage());
        }

        DataSource dataSource;
        try {
            dataSource = dataSourceService.getDataSource(dataSourceName);
        } catch (Exception e) {
            log.error("Failed to get data source: {}", dataSourceName, e);
            return SqlQueryResult.error("数据源不存在: " + dataSourceName);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ResultSetMetaData metaData = null;

        try {
            connection = DataSourceUtils.getConnection(dataSource);
            preparedStatement = connection.prepareStatement(sql);

            // 设置参数
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    preparedStatement.setObject(i + 1, params.get(i));
                }
            }

            resultSet = preparedStatement.executeQuery();
            metaData = resultSet.getMetaData();

            // 获取列信息
            int columnCount = metaData.getColumnCount();
            List<ColumnInfo> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(new ColumnInfo(
                        metaData.getColumnName(i),
                        metaData.getColumnTypeName(i),
                        metaData.getColumnDisplaySize(i)
                ));
            }

            // 获取数据
            List<Map<String, Object>> rows = new ArrayList<>();
            int rowCount = 0;
            while (resultSet.next() && rowCount < 1000) { // 限制最多1000行
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                rows.add(row);
                rowCount++;
            }

            // 检查是否还有更多数据
            boolean hasMore = resultSet.next();

            return SqlQueryResult.success(columns, rows, rowCount, hasMore);

        } catch (SQLException e) {
            log.error("SQL execution error", e);
            return SqlQueryResult.error("SQL执行错误: " + e.getMessage());
        } finally {
            // 关闭资源
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (SQLException e) {
                log.error("Error closing resources", e);
            }
        }
    }

    /**
     * 验证SQL语句
     * 只允许SELECT查询语句
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
            return SqlValidationResult.invalid("只允许执行SELECT查询语句");
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
     * SQL查询结果
     */
    public static class SqlQueryResult {
        private boolean success;
        private String message;
        private List<ColumnInfo> columns;
        private List<Map<String, Object>> data;
        private int rowCount;
        private boolean hasMore;

        public static SqlQueryResult success(List<ColumnInfo> columns, 
                                           List<Map<String, Object>> data, 
                                           int rowCount, 
                                           boolean hasMore) {
            SqlQueryResult result = new SqlQueryResult();
            result.success = true;
            result.message = "查询成功";
            result.columns = columns;
            result.data = data;
            result.rowCount = rowCount;
            result.hasMore = hasMore;
            return result;
        }

        public static SqlQueryResult error(String message) {
            SqlQueryResult result = new SqlQueryResult();
            result.success = false;
            result.message = message;
            return result;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<ColumnInfo> getColumns() { return columns; }
        public List<Map<String, Object>> getData() { return data; }
        public int getRowCount() { return rowCount; }
        public boolean isHasMore() { return hasMore; }
    }

    /**
     * 列信息
     */
    public static class ColumnInfo {
        private String name;
        private String type;
        private int width;

        public ColumnInfo(String name, String type, int width) {
            this.name = name;
            this.type = type;
            this.width = width;
        }

        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public int getWidth() { return width; }
    }
}