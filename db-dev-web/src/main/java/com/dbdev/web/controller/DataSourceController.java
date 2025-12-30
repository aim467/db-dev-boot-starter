package com.dbdev.web.controller;

import com.dbdev.core.model.DataSourceInfo;
import com.dbdev.core.model.PoolStats;
import com.dbdev.core.service.DataSourceService;
import com.dbdev.core.service.PoolStatsService;
import com.dbdev.core.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源管理 Controller
 * 实际路径：{uiPath}/api/datasource
 */
@RestController
@RequestMapping("/api/datasource")
@RequiredArgsConstructor
public class DataSourceController extends BaseController {
    
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
}
