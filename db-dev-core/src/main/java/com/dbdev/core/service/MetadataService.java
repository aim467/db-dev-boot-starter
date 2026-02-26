package com.dbdev.core.service;

import com.dbdev.core.model.ColumnMetadata;
import com.dbdev.core.model.DatabaseMetadata;
import com.dbdev.core.model.IndexMetadata;
import com.dbdev.core.model.TableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 数据库元数据服务
 */
@Slf4j
@Service
public class MetadataService {

    /**
     * 获取数据库元数据
     */
    public DatabaseMetadata getDatabaseMetadata(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            return DatabaseMetadata.builder()
                    .databaseName(connection.getCatalog())
                    .productName(metaData.getDatabaseProductName())
                    .productVersion(metaData.getDatabaseProductVersion())
                    .tables(getTables(metaData, connection.getCatalog()))
                    .build();
        }
    }

    /**
     * 获取表列表
     */
    public List<TableMetadata> getTables(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return getTables(metaData, connection.getCatalog());
        }
    }

    private List<TableMetadata> getTables(DatabaseMetaData metaData, String catalog) throws SQLException {
        List<TableMetadata> tables = new ArrayList<>();

        try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tables.add(TableMetadata.builder()
                        .tableName(tableName)
                        .tableType(rs.getString("TABLE_TYPE"))
                        .remarks(rs.getString("REMARKS"))
                        .build());
            }
        }

        return tables;
    }

    /**
     * 获取表详细信息
     */
    public TableMetadata getTableDetail(DataSource dataSource, String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();

            return TableMetadata.builder()
                    .tableName(tableName)
                    .columns(getColumns(metaData, catalog, tableName))
                    .indexes(getIndexes(metaData, catalog, tableName))
                    .primaryKeys(getPrimaryKeys(metaData, catalog, tableName))
                    .build();
        }
    }

    /**
     * 获取字段列表
     */
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
                        .javaType(getJavaType(rs.getString("TYPE_NAME")))
                        .build());
            }
        }

        return columns;
    }


    /**
     * 根据 dataType 获取 Java 类型
     */
    public static String getJavaType(String dataType) {
        switch (dataType) {
            case "VARCHAR":
            case "CHAR":
            case "TEXT":
            case "NVARCHAR":
            case "NCHAR":
            case "LONGVARCHAR":
            case "LONGTEXT":
                return "String";
            case "INT":
            case "INTEGER":
            case "BIGINT":
            case "SMALLINT":
            case "TINYINT":
                return "Integer";
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
                return "Double";
            case "BOOLEAN":
                return "Boolean";
            case "DATE":
            case "DATETIME":
            case "TIMESTAMP":
            case "TIME":
                return "Date";
            default:
                return "Object";
        }
    }


    /**
     * 获取索引列表
     */
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

    /**
     * 获取主键列表
     */
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

    /**
     * 创建表
     */
    public void createTable(DataSource dataSource, TableMetadata tableMetadata) throws SQLException {
        if (tableMetadata == null) {
            throw new SQLException("Table metadata must not be null");
        }
        if (tableMetadata.getTableName() == null || tableMetadata.getTableName().trim().isEmpty()) {
            throw new SQLException("Table name must not be empty");
        }
        if (tableMetadata.getColumns() == null || tableMetadata.getColumns().isEmpty()) {
            throw new SQLException("Table must have at least one column");
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String productName = connection.getMetaData().getDatabaseProductName();
            boolean isMySql = productName != null
                    && productName.toLowerCase(Locale.ROOT).contains("mysql");

            List<String> columnDefinitions = new ArrayList<>();
            List<String> primaryKeys = new ArrayList<>();

            for (ColumnMetadata column : tableMetadata.getColumns()) {
                if (column.getColumnName() == null || column.getColumnName().trim().isEmpty()) {
                    throw new SQLException("Column name must not be empty");
                }

                StringBuilder columnDef = new StringBuilder();
                columnDef.append(quoteIdentifier(column.getColumnName(), isMySql))
                        .append(" ")
                        .append(buildColumnType(column));

                if (!column.isNullable()) {
                    columnDef.append(" NOT NULL");
                }

                if (column.getDefaultValue() != null && !column.getDefaultValue().isEmpty()) {
                    columnDef.append(" DEFAULT ").append(column.getDefaultValue());
                }

                if (column.isAutoIncrement() && isMySql) {
                    columnDef.append(" AUTO_INCREMENT");
                }

                columnDefinitions.add(columnDef.toString());

                if (column.isPrimaryKey()) {
                    primaryKeys.add(quoteIdentifier(column.getColumnName(), isMySql));
                }
            }

            if (!primaryKeys.isEmpty()) {
                columnDefinitions.add("PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
            }

            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ")
                    .append(quoteIdentifier(tableMetadata.getTableName(), isMySql))
                    .append(" (")
                    .append(String.join(", ", columnDefinitions))
                    .append(")");

            if (tableMetadata.getRemarks() != null
                    && !tableMetadata.getRemarks().trim().isEmpty()
                    && isMySql) {
                sql.append(" COMMENT '")
                        .append(tableMetadata.getRemarks().replace("'", "''"))
                        .append("'");
            }

            String createSql = sql.toString();
            log.info("Creating table with SQL: {}", createSql);
            statement.executeUpdate(createSql);
        }
    }

    private String buildColumnType(ColumnMetadata column) {
        String typeName = column.getTypeName();
        if (typeName == null || typeName.trim().isEmpty()) {
            typeName = column.getDataType();
        }
        if (typeName == null || typeName.trim().isEmpty()) {
            typeName = "VARCHAR";
        }
        typeName = typeName.toUpperCase(Locale.ROOT);

        Integer size = column.getColumnSize();
        Integer scale = column.getDecimalDigits();

        if (size != null && size > 0) {
            if (scale != null && scale > 0) {
                return typeName + "(" + size + "," + scale + ")";
            }
            return typeName + "(" + size + ")";
        }

        return typeName;
    }

    private String quoteIdentifier(String identifier, boolean isMySql) {
        if (isMySql) {
            return "`" + identifier.replace("`", "``") + "`";
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
