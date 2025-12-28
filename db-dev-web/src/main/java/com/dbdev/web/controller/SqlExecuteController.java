package com.dbdev.web.controller;

import com.dbdev.core.service.SqlExecuteService;
import com.dbdev.web.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SQL执行 Controller
 * 实际路径：{uiPath}/api/sql
 */
@RestController
@RequestMapping("/api/sql")
@RequiredArgsConstructor
public class SqlExecuteController extends BaseController {
    
    private final SqlExecuteService sqlExecuteService;
    
    /**
     * 执行SQL查询
     *
     * @param request SQL查询请求
     * @return 查询结果
     */
    @PostMapping("/execute")
    public Result<SqlQueryResponse> executeQuery(@RequestBody SqlQueryRequest request) {
        if (request.getDataSourceName() == null || request.getDataSourceName().trim().isEmpty()) {
            return Result.error("数据源名称不能为空");
        }
        
        if (request.getSql() == null || request.getSql().trim().isEmpty()) {
            return Result.error("SQL语句不能为空");
        }
        
        SqlExecuteService.SqlQueryResult result = sqlExecuteService.executeQuery(
                request.getDataSourceName(), 
                request.getSql(), 
                request.getParams()
        );
        
        if (result.isSuccess()) {
            SqlQueryResponse response = new SqlQueryResponse();
            response.setColumns(result.getColumns());
            response.setData(result.getData());
            response.setRowCount(result.getRowCount());
            response.setHasMore(result.isHasMore());
            return Result.success(response);
        } else {
            return Result.error(result.getMessage());
        }
    }
    
    /**
     * SQL查询请求
     */
    public static class SqlQueryRequest {
        private String dataSourceName;
        private String sql;
        private List<Object> params;
        
        public String getDataSourceName() {
            return dataSourceName;
        }
        
        public void setDataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
        }
        
        public String getSql() {
            return sql;
        }
        
        public void setSql(String sql) {
            this.sql = sql;
        }
        
        public List<Object> getParams() {
            return params;
        }
        
        public void setParams(List<Object> params) {
            this.params = params;
        }
    }
    
    /**
     * SQL查询响应
     */
    public static class SqlQueryResponse {
        private List<SqlExecuteService.ColumnInfo> columns;
        private List<Map<String, Object>> data;
        private int rowCount;
        private boolean hasMore;
        
        public List<SqlExecuteService.ColumnInfo> getColumns() {
            return columns;
        }
        
        public void setColumns(List<SqlExecuteService.ColumnInfo> columns) {
            this.columns = columns;
        }
        
        public List<Map<String, Object>> getData() {
            return data;
        }
        
        public void setData(List<Map<String, Object>> data) {
            this.data = data;
        }
        
        public int getRowCount() {
            return rowCount;
        }
        
        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }
        
        public boolean isHasMore() {
            return hasMore;
        }
        
        public void setHasMore(boolean hasMore) {
            this.hasMore = hasMore;
        }
    }
}