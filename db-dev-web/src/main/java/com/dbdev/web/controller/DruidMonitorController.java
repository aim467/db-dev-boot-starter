package com.dbdev.web.controller;

import com.dbdev.core.druid.DruidDataSourceProvider;
import com.dbdev.core.response.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Druid 监控 Controller
 * 仅当检测到 Druid 数据源时才启用
 */
@Slf4j
@RestController
@RequestMapping("/api/druid")
@RequiredArgsConstructor
public class DruidMonitorController extends BaseController {

    @Autowired
    private DruidDataSourceProvider druidDataSourceProvider;

    /**
     * 检查 Druid 是否启用
     */
    @GetMapping("/enabled")
    public Result<Map<String, Object>> isDruidEnabled() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", druidDataSourceProvider.isDruidDataSource());
        data.put("type", "Alibaba Druid");
        return Result.success(data);
    }

    /**
     * 获取连接池状态
     */
    @GetMapping("/pool-stats")
    public Result<Map<String, Object>> getPoolStats() {
        try {
            Map<String, Object> stats = druidDataSourceProvider.getPoolStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("Failed to get Druid pool stats", e);
            return Result.error("获取连接池状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取 SQL 统计信息
     */
    @GetMapping("/sql-stats")
    public Result<List<Map<String, Object>>> getSqlStats() {
        try {
            List<Map<String, Object>> stats = druidDataSourceProvider.getSqlStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("Failed to get SQL stats", e);
            return Result.error("获取 SQL 统计失败: " + e.getMessage());
        }
    }

    /**
     * 重置统计信息
     */
    @PostMapping("/reset-stats")
    public Result<String> resetStats() {
        try {
            druidDataSourceProvider.resetStats();
            return Result.success("统计信息已重置");
        } catch (Exception e) {
            log.error("Failed to reset stats", e);
            return Result.error("重置统计失败: " + e.getMessage());
        }
    }
}
