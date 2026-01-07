package com.dbdev.core.service;

import com.dbdev.core.model.*;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SchemaExportService {

    private final DataSourceService dataSourceService;

    /**
     * 导出为 Markdown 格式
     */
    public String exportToMarkdown(String dataSourceName) throws SQLException {
        DataSource dataSource = dataSourceService.getDataSource(dataSourceName);
        DatabaseMetadata metadata = getFullMetadata(dataSource);
        
        StringBuilder sb = new StringBuilder();
        
        // 标题
        sb.append("# 数据库表结构文档\n\n");
        sb.append("> 生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        sb.append("> 数据库: ").append(metadata.getProductName()).append(" ").append(metadata.getProductVersion()).append("\n\n");
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
     * 导出为 HTML 格式
     */
    public String exportToHtml(String dataSourceName) throws SQLException {
        DataSource dataSource = dataSourceService.getDataSource(dataSourceName);
        DatabaseMetadata metadata = getFullMetadata(dataSource);
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"zh-CN\">\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("    <title>数据库表结构文档</title>\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 40px; background: #f5f5f5; }\n");
        sb.append("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n");
        sb.append("        h1 { color: #333; border-bottom: 3px solid #409eff; padding-bottom: 10px; }\n");
        sb.append("        h2 { color: #409eff; margin-top: 30px; }\n");
        sb.append("        h3 { color: #606266; }\n");
        sb.append("        .info { background: #ecf5ff; padding: 15px; border-radius: 4px; margin: 20px 0; color: #409eff; }\n");
        sb.append("        table { width: 100%; border-collapse: collapse; margin: 15px 0; }\n");
        sb.append("        th, td { border: 1px solid #dcdfe6; padding: 12px; text-align: left; }\n");
        sb.append("        th { background: #409eff; color: white; }\n");
        sb.append("        tr:nth-child(even) { background: #fafafa; }\n");
        sb.append("        tr:hover { background: #ecf5ff; }\n");
        sb.append("        .primary-key { background: #fdf6ec; }\n");
        sb.append("        .index { background: #f0f9eb; padding: 10px; margin: 10px 0; border-radius: 4px; }\n");
        sb.append("        .table-section { margin: 30px 0; padding: 20px; border: 1px solid #e4e7ed; border-radius: 8px; }\n");
        sb.append("        .table-name { color: #409eff; font-size: 18px; margin-bottom: 10px; }\n");
        sb.append("        .remark { color: #909399; font-style: italic; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <div class=\"container\">\n");
        sb.append("        <h1>数据库表结构文档</h1>\n");
        sb.append("        <div class=\"info\">\n");
        sb.append("            <p><strong>数据库:</strong> ").append(metadata.getProductName()).append(" ").append(metadata.getProductVersion()).append("</p>\n");
        sb.append("            <p><strong>生成时间:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");
        sb.append("            <p><strong>表数量:</strong> ").append(metadata.getTables().size()).append("</p>\n");
        sb.append("        </div>\n");
        
        for (TableMetadata table : metadata.getTables()) {
            sb.append("        <div class=\"table-section\">\n");
            sb.append("            <h2 class=\"table-name\">").append(table.getTableName());
            if (table.getRemarks() != null && !table.getRemarks().isEmpty()) {
                sb.append(" <span class=\"remark\">- ").append(table.getRemarks()).append("</span>");
            }
            sb.append("</h2>\n");
            
            // 字段列表
            sb.append("            <h3>字段列表</h3>\n");
            sb.append("            <table>\n");
            sb.append("                <thead><tr><th>字段名</th><th>类型</th><th>可空</th><th>默认值</th><th>自增</th><th>备注</th></tr></thead>\n");
            sb.append("                <tbody>\n");
            
            if (table.getColumns() != null) {
                for (ColumnMetadata col : table.getColumns()) {
                    String rowClass = col.isPrimaryKey() ? "class=\"primary-key\"" : "";
                    sb.append("                    <tr ").append(rowClass).append("><td>").append(col.getColumnName());
                    if (col.isPrimaryKey()) sb.append(" 🔑");
                    sb.append("</td><td>").append(formatColumnType(col));
                    sb.append("</td><td>").append(col.isNullable() ? "是" : "否");
                    sb.append("</td><td>").append(col.getDefaultValue() != null ? escapeHtml(col.getDefaultValue()) : "-");
                    sb.append("</td><td>").append(col.isAutoIncrement() ? "是" : "否");
                    sb.append("</td><td>").append(col.getRemarks() != null ? escapeHtml(col.getRemarks()) : "-").append("</td></tr>\n");
                }
            }
            sb.append("                </tbody>\n");
            sb.append("            </table>\n");
            
            // 主键
            if (table.getPrimaryKeys() != null && !table.getPrimaryKeys().isEmpty()) {
                sb.append("            <p><strong>主键:</strong> ").append(String.join(", ", table.getPrimaryKeys())).append("</p>\n");
            }
            
            // 索引
            if (table.getIndexes() != null && !table.getIndexes().isEmpty()) {
                sb.append("            <h3>索引</h3>\n");
                Map<String, List<IndexMetadata>> indexMap = new LinkedHashMap<>();
                for (IndexMetadata idx : table.getIndexes()) {
                    indexMap.computeIfAbsent(idx.getIndexName(), k -> new ArrayList<>()).add(idx);
                }
                
                sb.append("            <div class=\"index\">\n");
                for (Map.Entry<String, List<IndexMetadata>> entry : indexMap.entrySet()) {
                    List<String> cols = new ArrayList<>();
                    for (IndexMetadata idx : entry.getValue()) {
                        if (idx.getColumnName() != null) {
                            cols.add(idx.getColumnName());
                        }
                    }
                    sb.append("                <p>• <strong>").append(entry.getKey()).append("</strong>: ");
                    sb.append(String.join(", ")).append(entry.getValue().get(0).isUnique() ? " (唯一)" : "").append("</p>\n");
                }
                sb.append("            </div>\n");
            }
            
            sb.append("        </div>\n");
        }
        
        sb.append("    </div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        
        return sb.toString();
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
            try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
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
        if (text == null) return null;
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}