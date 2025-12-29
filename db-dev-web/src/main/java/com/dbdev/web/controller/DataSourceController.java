package com.dbdev.web.controller;

import com.dbdev.core.model.DataSourceInfo;
import com.dbdev.core.service.DataSourceService;
import com.dbdev.core.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    /**
     * 获取所有数据源
     */
    @GetMapping("/list")
    public Result<List<DataSourceInfo>> listDataSources() {
        List<DataSourceInfo> dataSources = dataSourceService.getAllDataSources();
        return Result.success(dataSources);
    }
}
