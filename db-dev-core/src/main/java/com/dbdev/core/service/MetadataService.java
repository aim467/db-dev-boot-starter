package com.dbdev.core.service;

import com.dbdev.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                        .build());
            }
        }
        
        return columns;
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
}
