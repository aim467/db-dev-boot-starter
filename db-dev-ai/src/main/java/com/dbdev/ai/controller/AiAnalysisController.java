package com.dbdev.ai.controller;

import com.dbdev.ai.model.AiAnalysisResult;
import com.dbdev.ai.service.AiAnalysisService;
import com.dbdev.core.model.TableMetadata;
import com.dbdev.core.response.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@ConditionalOnProperty(prefix = "db.dev.ai", name = "enabled", havingValue = "true")
public class AiAnalysisController {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    /**
     * 检查AI功能是否可用
     */
    @GetMapping("/status")
    public Result<AiStatusResponse> getStatus() {
        boolean enabled = aiAnalysisService.isEnabled();
        return Result.success(new AiStatusResponse(enabled,
                enabled ? "AI功能已启用" : "AI功能未启用或未配置"));
    }

    /**
     * 分析SQL语句
     */
    @PostMapping("/analyze/sql")
    public Result<AiAnalysisResult> analyzeSql(@RequestBody SqlAnalysisRequest request) {
        log.info("收到SQL分析请求: databaseType={}, sql长度={}",
                request.getDatabaseType(), request.getSql().length());

        if (request.getSql() == null || request.getSql().trim().isEmpty()) {
            return Result.error("SQL语句不能为空");
        }

        AiAnalysisResult result = aiAnalysisService.analyzeSql(
                request.getSql(),
                request.getDatabaseType() != null ? request.getDatabaseType() : "MySQL"
        );

        if (!result.isSuccess()) {
            return Result.error(result.getError());
        }

        return Result.success(result);
    }

    /**
     * 分析SQL执行计划
     */
    @PostMapping("/analyze/explain")
    public Result<AiAnalysisResult> analyzeExplain(@RequestBody ExplainAnalysisRequest request) {
        log.info("收到执行计划分析请求: databaseType={}", request.getDatabaseType());

        if (request.getExplainData() == null || request.getExplainData().isEmpty()) {
            return Result.error("执行计划数据不能为空");
        }

        AiAnalysisResult result = aiAnalysisService.analyzeExplain(
                request.getExplainData(),
                request.getDatabaseType() != null ? request.getDatabaseType() : "MySQL"
        );

        if (!result.isSuccess()) {
            return Result.error(result.getError());
        }

        return Result.success(result);
    }

    /**
     * 分析表结构
     */
    @PostMapping("/analyze/table")
    public Result<AiAnalysisResult> analyzeTable(@RequestBody TableAnalysisRequest request) {
        log.info("收到表结构分析请求: databaseType={}, 表数量={}",
                request.getDatabaseType(), request.getTables().size());

        if (request.getTables() == null || request.getTables().isEmpty()) {
            return Result.error("表结构数据不能为空");
        }

        AiAnalysisResult result = aiAnalysisService.analyzeTableStructure(
                request.getTables(),
                request.getDatabaseType() != null ? request.getDatabaseType() : "MySQL"
        );

        if (!result.isSuccess()) {
            return Result.error(result.getError());
        }

        return Result.success(result);
    }

    /**
     * AI状态响应
     */
    @Data
    public static class AiStatusResponse {
        private boolean enabled;
        private String message;

        public AiStatusResponse(boolean enabled, String message) {
            this.enabled = enabled;
            this.message = message;
        }
    }

    /**
     * SQL分析请求
     */
    @Data
    public static class SqlAnalysisRequest {
        private String sql;
        private String databaseType;
    }

    /**
     * 执行计划分析请求
     */
    @Data
    public static class ExplainAnalysisRequest {
        private Map<String, Object> explainData;
        private String databaseType;
    }

    /**
     * 表结构分析请求
     */
    @Data
    public static class TableAnalysisRequest {
        private List<TableMetadata> tables;
        private String databaseType;
    }
}
