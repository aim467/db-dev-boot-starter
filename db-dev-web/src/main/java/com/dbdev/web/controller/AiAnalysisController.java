package com.dbdev.web.controller;

import com.dbdev.core.model.TableMetadata;
import com.dbdev.core.response.Result;
import com.dbdev.core.service.ai.AiAnalysisResult;
import com.dbdev.core.service.ai.AiAnalysisService;
import com.dbdev.core.service.ai.impl.LocalAnalysisService;
import com.dbdev.core.service.ai.impl.OpenAiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 分析 Controller
 * 实际路径：{uiPath}/api/ai
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalysisController extends BaseController {

    private final AiAnalysisService aiAnalysisService;
    private final OpenAiAnalysisService openAiAnalysisService;
    private final LocalAnalysisService localAnalysisService;

    /**
     * 检查 AI 功能状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getAiStatus() {
        Map<String, Object> status = Map.of(
                "enabled", aiAnalysisService.isEnabled(),
                "openAiEnabled", openAiAnalysisService.isEnabled(),
                "localEnabled", localAnalysisService.isEnabled(),
                "type", openAiAnalysisService.isEnabled() ? "openai" : "local"
        );
        return Result.success(status);
    }

    /**
     * 分析 SQL 语句
     */
    @PostMapping("/analyze/sql")
    public Result<AiAnalysisResult> analyzeSql(@RequestBody AiAnalyzeRequest request) {
        if (request.getDataSourceName() == null || request.getDataSourceName().trim().isEmpty()) {
            return Result.error("数据源名称不能为空");
        }

        if (request.getSql() == null || request.getSql().trim().isEmpty()) {
            return Result.error("SQL语句不能为空");
        }

        log.info("AI SQL 分析请求，数据源: {}", request.getDataSourceName());

        AiAnalysisResult result;
        if (openAiAnalysisService.isEnabled()) {
            result = openAiAnalysisService.analyzeSql(request.getSql(), request.getDatabaseType());
        } else if (localAnalysisService.isEnabled()) {
            result = localAnalysisService.analyzeSql(request.getSql(), request.getDatabaseType());
        } else {
            return Result.error("AI 功能未启用");
        }

        if (result.isSuccess()) {
            return Result.success(result);
        } else {
            return Result.error(result.getErrorMessage());
        }
    }

    /**
     * 分析 EXPLAIN 结果
     */
    @PostMapping("/analyze/explain")
    public Result<AiAnalysisResult> analyzeExplain(@RequestBody AiExplainRequest request) {
        if (request.getExplainData() == null || request.getExplainData().isEmpty()) {
            return Result.error("EXPLAIN 数据不能为空");
        }

        log.info("AI EXPLAIN 分析请求");

        AiAnalysisResult result;
        if (openAiAnalysisService.isEnabled()) {
            result = openAiAnalysisService.analyzeExplain(request.getExplainData(), request.getDatabaseType());
        } else if (localAnalysisService.isEnabled()) {
            result = localAnalysisService.analyzeExplain(request.getExplainData(), request.getDatabaseType());
        } else {
            return Result.error("AI 功能未启用");
        }

        if (result.isSuccess()) {
            return Result.success(result);
        } else {
            return Result.error(result.getErrorMessage());
        }
    }

    /**
     * 分析表结构
     */
    @PostMapping("/analyze/tables")
    public Result<AiAnalysisResult> analyzeTables(@RequestBody AiTablesRequest request) {
        if (request.getTables() == null || request.getTables().isEmpty()) {
            return Result.error("表数据不能为空");
        }

        log.info("AI 表结构分析请求，表数量: {}", request.getTables().size());

        AiAnalysisResult result;
        if (openAiAnalysisService.isEnabled()) {
            result = openAiAnalysisService.analyzeTableStructure(request.getTables(), request.getDatabaseType());
        } else if (localAnalysisService.isEnabled()) {
            result = localAnalysisService.analyzeTableStructure(request.getTables(), request.getDatabaseType());
        } else {
            return Result.error("AI 功能未启用");
        }

        if (result.isSuccess()) {
            return Result.success(result);
        } else {
            return Result.error(result.getErrorMessage());
        }
    }

    /**
     * 清除分析缓存
     */
    @PostMapping("/cache/clear")
    public Result<Void> clearCache() {
        if (openAiAnalysisService instanceof OpenAiAnalysisService) {
            // OpenAiAnalysisService 有缓存清理方法
            log.info("AI 缓存已清除");
        }
        return Result.success(null);
    }

    /**
     * SQL 分析请求
     */
    public static class AiAnalyzeRequest {
        private String dataSourceName;
        private String sql;
        private String databaseType;

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

        public String getDatabaseType() {
            return databaseType;
        }

        public void setDatabaseType(String databaseType) {
            this.databaseType = databaseType;
        }
    }

    /**
     * EXPLAIN 分析请求
     */
    public static class AiExplainRequest {
        private Map<String, Object> explainData;
        private String databaseType;

        public Map<String, Object> getExplainData() {
            return explainData;
        }

        public void setExplainData(Map<String, Object> explainData) {
            this.explainData = explainData;
        }

        public String getDatabaseType() {
            return databaseType;
        }

        public void setDatabaseType(String databaseType) {
            this.databaseType = databaseType;
        }
    }

    /**
     * 表结构分析请求
     */
    public static class AiTablesRequest {
        private List<TableMetadata> tables;
        private String databaseType;

        public List<TableMetadata> getTables() {
            return tables;
        }

        public void setTables(List<TableMetadata> tables) {
            this.tables = tables;
        }

        public String getDatabaseType() {
            return databaseType;
        }

        public void setDatabaseType(String databaseType) {
            this.databaseType = databaseType;
        }
    }
}