package com.dbdev.core.service;

import com.dbdev.core.model.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据库表结构导出服务
 */
@Slf4j
@Service
public class SchemaExportService {

    private final DataSourceService dataSourceService;
    private final Configuration freeMarkerConfiguration;

    public SchemaExportService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
        this.freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_32);
        this.freeMarkerConfiguration.setClassForTemplateLoading(this.getClass(), "/templates");
        this.freeMarkerConfiguration.setDefaultEncoding("UTF-8");
    }

    /**
     * 导出为 Markdown 格式
     */
    public String exportToMarkdown(String dataSourceName) throws SQLException {
        DataSource dataSource = dataSourceService.getDataSource(dataSourceName);
        DatabaseMetadata metadata = getFullMetadata(dataSource);

        StringBuilder sb = new StringBuilder();

        // 标题
        sb.append("# 数据库表结构文档\n\n");
        sb.append("> 生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");
        sb.append("> 数据库: ").append(metadata.getProductName()).append(" ").append(metadata.getProductVersion())
                .append("\n\n");
        sb.append("> 表数量: ").append(metadata.getTables().size()).append("\n\n");
        sb.append("---\n\n");

        // 遍历所有表
        for (TableMetadata table : metadata.getTables()) {
            sb.append("## ").append(table.getTableName());
            if (table.getRemarks() != null && !table.getRemarks().isEmpty()) {
                sb.append(" - ").append(table.getRemarks());
            }
            sb.append("\n\n");

            // 表信息
            sb.append("**类型**: ").append(table.getTableType()).append("\n\n");

            // 字段列表
            sb.append("### 字段列表\n\n");
            sb.append("| 字段名 | 类型 | 可空 | 默认值 | 备注 |\n");
            sb.append("|--------|------|------|--------|------|\n");

            if (table.getColumns() != null) {
                for (ColumnMetadata col : table.getColumns()) {
                    sb.append("| ").append(col.getColumnName());
                    sb.append(" | ").append(formatColumnType(col));
                    sb.append(" | ").append(col.isNullable() ? "是" : "否");
                    sb.append(" | ").append(col.getDefaultValue() != null ? col.getDefaultValue() : "-");
                    sb.append(" | ").append(col.getRemarks() != null ? col.getRemarks() : "-");
                    sb.append(" |\n");
                }
            }
            sb.append("\n");

            // 主键
            if (table.getPrimaryKeys() != null && !table.getPrimaryKeys().isEmpty()) {
                sb.append("**主键**: ").append(String.join(", ", table.getPrimaryKeys())).append("\n\n");
            }

            // 索引
            if (table.getIndexes() != null && !table.getIndexes().isEmpty()) {
                sb.append("### 索引\n\n");
                Map<String, List<IndexMetadata>> indexMap = new LinkedHashMap<>();
                for (IndexMetadata idx : table.getIndexes()) {
                    indexMap.computeIfAbsent(idx.getIndexName(), k -> new ArrayList<>()).add(idx);
                }

                for (Map.Entry<String, List<IndexMetadata>> entry : indexMap.entrySet()) {
                    List<String> cols = new ArrayList<>();
                    for (IndexMetadata idx : entry.getValue()) {
                        if (idx.getColumnName() != null) {
                            cols.add(idx.getColumnName());
                        }
                    }
                    sb.append("- **").append(entry.getKey()).append("**: ").append(String.join(", "));
                    sb.append(entry.getValue().get(0).isUnique() ? " (唯一)" : "").append("\n");
                }
                sb.append("\n");
            }

            sb.append("---\n\n");
        }

        return sb.toString();
    }

    /**
     * 导出为 HTML 格式（使用 FreeMarker 模板）
     */
    public String exportToHtml(String dataSourceName) {
        try {
            DataSource dataSource = dataSourceService.getDataSource(dataSourceName);
            DatabaseMetadata metadata = getFullMetadata(dataSource);

            // 预处理数据：添加 formattedType 字段
            for (TableMetadata table : metadata.getTables()) {
                if (table.getColumns() != null) {
                    for (ColumnMetadata col : table.getColumns()) {
                        col.setFormattedType(formatColumnType(col));
                    }
                }
            }

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("metadata", metadata);
            templateData.put("generateTime",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Template template = freeMarkerConfiguration.getTemplate("schema-export.ftl");
            StringWriter writer = new StringWriter();
            template.process(templateData, writer);
            return writer.toString();
        } catch (Exception exception) {
            log.error("Failed to export to HTML: {}", exception.getMessage());
        }
        return "";
    }

    /**
     * 获取完整的数据库元数据（包含所有表的详细信息）
     */

    private DatabaseMetadata getFullMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();

            List<TableMetadata> tables = new ArrayList<>();

            // 获取所有表
            try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[] { "TABLE" })) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    TableMetadata table = TableMetadata.builder()
                            .tableName(tableName)
                            .tableType(rs.getString("TABLE_TYPE"))
                            .remarks(rs.getString("REMARKS"))
                            .columns(getColumns(metaData, catalog, tableName))
                            .indexes(getIndexes(metaData, catalog, tableName))
                            .primaryKeys(getPrimaryKeys(metaData, catalog, tableName))
                            .build();
                    tables.add(table);
                }
            }

            return DatabaseMetadata.builder()
                    .databaseName(catalog)
                    .productName(metaData.getDatabaseProductName())
                    .productVersion(metaData.getDatabaseProductVersion())
                    .tables(tables)
                    .build();
        }
    }

    private List<ColumnMetadata> getColumns(DatabaseMetaData metaData, String catalog, String tableName)
            throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();

        try (ResultSet rs = metaData.getColumns(catalog, null, tableName, "%")) {
            while (rs.next()) {
                columns.add(ColumnMetadata.builder()
                        .columnName(rs.getString("COLUMN_NAME"))
                        .dataType(rs.getString("DATA_TYPE"))
                        .typeName(rs.getString("TYPE_NAME"))
                        .columnSize(rs.getInt("COLUMN_SIZE"))
                        .decimalDigits(rs.getInt("DECIMAL_DIGITS"))
                        .nullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                        .defaultValue(rs.getString("COLUMN_DEF"))
                        .remarks(rs.getString("REMARKS"))
                        .autoIncrement("YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")))
                        .javaType(MetadataService.getJavaType(rs.getString("TYPE_NAME")))
                        .build());
            }
        }

        // 标记主键
        List<String> pkColumns = getPrimaryKeys(metaData, catalog, tableName);
        for (ColumnMetadata col : columns) {
            col.setPrimaryKey(pkColumns.contains(col.getColumnName()));
        }

        return columns;
    }

    private List<IndexMetadata> getIndexes(DatabaseMetaData metaData, String catalog, String tableName)
            throws SQLException {
        List<IndexMetadata> indexes = new ArrayList<>();

        try (ResultSet rs = metaData.getIndexInfo(catalog, null, tableName, false, false)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName != null) {
                    indexes.add(IndexMetadata.builder()
                            .indexName(indexName)
                            .columnName(rs.getString("COLUMN_NAME"))
                            .unique(!rs.getBoolean("NON_UNIQUE"))
                            .type(String.valueOf(rs.getShort("TYPE")))
                            .build());
                }
            }
        }

        return indexes;
    }

    private List<String> getPrimaryKeys(DatabaseMetaData metaData, String catalog, String tableName)
            throws SQLException {
        List<String> primaryKeys = new ArrayList<>();

        try (ResultSet rs = metaData.getPrimaryKeys(catalog, null, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }

        return primaryKeys;
    }

    private String formatColumnType(ColumnMetadata col) {
        String type = col.getTypeName();
        if (col.getColumnSize() > 0 && col.getColumnSize() < 10000) {
            if (col.getDecimalDigits() != null && col.getDecimalDigits() > 0) {
                return type + "(" + col.getColumnSize() + "," + col.getDecimalDigits() + ")";
            }
            return type + "(" + col.getColumnSize() + ")";
        }
        return type;
    }

    private String escapeHtml(String text) {
        if (text == null)
            return null;
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}