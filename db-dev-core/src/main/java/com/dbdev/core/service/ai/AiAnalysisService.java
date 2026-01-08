package com.dbdev.core.service.ai;

import com.dbdev.core.model.TableMetadata;

import java.util.List;
import java.util.Map;

/**
 * AI 分析服务接口
 */
public interface AiAnalysisService {

    /**
     * 分析 SQL 语句
     *
     * @param sql SQL 语句
     * @param databaseType 数据库类型
     * @return 分析结果
     */
    AiAnalysisResult analyzeSql(String sql, String databaseType);

    /**
     * 分析 EXPLAIN 结果
     *
     * @param explainData EXPLAIN 数据
     * @param databaseType 数据库类型
     * @return 分析结果
     */
    AiAnalysisResult analyzeExplain(Map<String, Object> explainData, String databaseType);

    /**
     * 分析表结构并提供优化建议
     *
     * @param tables 表元数据列表
     * @param databaseType 数据库类型
     * @return 分析结果
     */
    AiAnalysisResult analyzeTableStructure(List<TableMetadata> tables, String databaseType);

    /**
     * 检查 AI 功能是否启用
     *
     * @return 是否启用
     */
    boolean isEnabled();
}