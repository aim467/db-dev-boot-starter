package com.dbdev.web.controller;

import com.dbdev.core.model.DataSourceInfo;
import com.dbdev.core.model.PoolStats;
import com.dbdev.core.response.Result;
import com.dbdev.core.service.DataSourceService;
import com.dbdev.core.service.PoolStatsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据源管理 Controller
 * 实际路径：{uiPath}/api/datasource
 */
@RestController
@RequestMapping("/api/datasource")
@RequiredArgsConstructor
public class DataSourceController {
    
    private final DataSourceService dataSourceService;
    private final PoolStatsService poolStatsService;
    
    /**
     * 获取所有数据源
     */
    @GetMapping("/list")
    public Result<List<DataSourceInfo>> listDataSources() {
        List<DataSourceInfo> dataSources = dataSourceService.getAllDataSources();
        return Result.success(dataSources);
    }
    
    /**
     * 获取连接池统计信息
     */
    @GetMapping("/pool-stats")
    public Result<PoolStats> getPoolStats(@RequestParam String dataSourceName) {
        try {
            PoolStats stats = poolStatsService.getPoolStats(dataSourceName);
            return Result.success(stats);
        } catch (Exception e) {
            Result<PoolStats> result = new Result<>();
            result.setCode(500);
            result.setMessage("获取连接池统计失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 测试数据源连接
     * @param request 数据源名称
     */
    @PostMapping("/test")
    public Result<Map<String, Object>> testConnection(@RequestBody TestConnectionRequest request) {
        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            return Result.error("数据源名称不能为空");
        }

        boolean success = dataSourceService.testConnection(request.getName().trim());
        return Result.success(Map.of(
                "name", request.getName().trim(),
                "success", success
        ));
    }

    @Data
    public static class TestConnectionRequest {
        private String name;
    }
}
