package com.dbdev.web.controller;

import com.dbdev.core.model.DatabaseMetadata;
import com.dbdev.core.model.TableMetadata;
import com.dbdev.core.service.DataSourceService;
import com.dbdev.core.service.MetadataService;
import com.dbdev.core.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * 元数据管理 Controller
 * 实际路径：{uiPath}/api/metadata
 */
@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController extends BaseController {

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
}
