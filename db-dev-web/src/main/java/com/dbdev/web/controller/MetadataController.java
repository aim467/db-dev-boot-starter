package com.dbdev.web.controller;

import com.dbdev.core.model.ColumnMetadata;
import com.dbdev.core.model.DatabaseMetadata;
import com.dbdev.core.model.TableMetadata;
import com.dbdev.core.response.Result;
import com.dbdev.core.service.DataSourceService;
import com.dbdev.core.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 元数据管理 Controller
 * 实际路径：{uiPath}/api/metadata
 */
@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;
    private final DataSourceService dataSourceService;

    /**
     * 获取数据库元数据
     */
    @GetMapping("/database")
    public Result<DatabaseMetadata> getDatabaseMetadata(
            @RequestParam String dataSourceName) {
        try {
            DataSource dataSource = dataSourceService.getDataSource(dataSourceName);
            DatabaseMetadata metadata = metadataService.getDatabaseMetadata(dataSource);
            return Result.success(metadata);
        } catch (SQLException e) {
            return Result.error("Failed to get database metadata: " + e.getMessage());
        }
    }

    /**
     * 获取表列表
     */
    @GetMapping("/tables")
    public Result<List<TableMetadata>> getTables(
            @RequestParam(defaultValue = "dataSource") String dataSourceName) {
        try {
            DataSource dataSource = dataSourceService.getDataSource(dataSourceName);
            List<TableMetadata> tables = metadataService.getTables(dataSource);
            return Result.success(tables);
        } catch (SQLException e) {
            return Result.error("Failed to get tables: " + e.getMessage());
        }
    }

    /**
     * 获取表详细信息
     */
    @GetMapping("/table/{tableName}")
    public Result<TableMetadata> getTableDetail(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "dataSource") String dataSourceName) {
        try {
            DataSource dataSource = dataSourceService.getDataSource(dataSourceName);
            TableMetadata table = metadataService.getTableDetail(dataSource, tableName);
            return Result.success(table);
        } catch (SQLException e) {
            return Result.error("Failed to get table detail: " + e.getMessage());
        }
    }

    /**
     * 创建表
     */
    @PostMapping("/table")
    public Result<Void> createTable(
            @RequestParam(defaultValue = "dataSource") String dataSourceName,
            @RequestBody CreateTableRequest request) {
        if (request == null || request.getTableName() == null
                || request.getTableName().trim().isEmpty()) {
            return Result.error("表名不能为空");
        }
        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            return Result.error("至少需要定义一个字段");
        }

        try {
            DataSource dataSource = dataSourceService.getDataSource(dataSourceName);
            TableMetadata tableMetadata = buildTableMetadata(request);
            metadataService.createTable(dataSource, tableMetadata);
            return Result.success(null);
        } catch (SQLException e) {
            return Result.error("创建表失败: " + e.getMessage());
        }
    }

    private TableMetadata buildTableMetadata(CreateTableRequest request) {
        List<ColumnMetadata> columns = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();

        for (CreateColumnRequest c : request.getColumns()) {
            ColumnMetadata column = ColumnMetadata.builder()
                    .columnName(c.getColumnName())
                    .typeName(c.getTypeName())
                    .columnSize(c.getColumnSize())
                    .decimalDigits(c.getDecimalDigits())
                    .nullable(c.isNullable())
                    .defaultValue(c.getDefaultValue())
                    .remarks(c.getRemarks())
                    .autoIncrement(c.isAutoIncrement())
                    .primaryKey(c.isPrimaryKey())
                    .build();
            columns.add(column);
            if (c.isPrimaryKey()) {
                primaryKeys.add(c.getColumnName());
            }
        }

        return TableMetadata.builder()
                .tableName(request.getTableName())
                .remarks(request.getRemarks())
                .columns(columns)
                .primaryKeys(primaryKeys)
                .build();
    }

    /**
     * 创建表请求
     */
    public static class CreateTableRequest {
        private String tableName;
        private String remarks;
        private List<CreateColumnRequest> columns;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public List<CreateColumnRequest> getColumns() {
            return columns;
        }

        public void setColumns(List<CreateColumnRequest> columns) {
            this.columns = columns;
        }
    }

    /**
     * 创建字段请求
     */
    public static class CreateColumnRequest {
        private String columnName;
        private String typeName;
        private Integer columnSize;
        private Integer decimalDigits;
        private boolean nullable = true;
        private String defaultValue;
        private String remarks;
        private boolean autoIncrement;
        private boolean primaryKey;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public Integer getColumnSize() {
            return columnSize;
        }

        public void setColumnSize(Integer columnSize) {
            this.columnSize = columnSize;
        }

        public Integer getDecimalDigits() {
            return decimalDigits;
        }

        public void setDecimalDigits(Integer decimalDigits) {
            this.decimalDigits = decimalDigits;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public boolean isAutoIncrement() {
            return autoIncrement;
        }

        public void setAutoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }
    }
}
