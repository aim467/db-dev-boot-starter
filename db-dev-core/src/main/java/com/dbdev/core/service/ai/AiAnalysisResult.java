package com.dbdev.core.service.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * AI 分析结果
 */
@Data
@Builder
public class AiAnalysisResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 分析摘要
     */
    private String summary;

    /**
     * 详细分析内容 (支持 Markdown)
     */
    private String details;

    /**
     * 问题列表
     */
    private List<AiIssue> issues;

    /**
     * 优化建议列表
     */
    private List<AiSuggestion> suggestions;

    /**
     * 相关 SQL 示例
     */
    private List<String> examples;

    /**
     * 风险等级: low / medium / high
     */
    private String riskLevel;

    /**
     * 预计性能影响
     */
    private String performanceImpact;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * AI 问题项
     */
    @Data
    @Builder
    public static class AiIssue {
        private String title;
        private String description;
        private String severity; // low / medium / high / critical
        private String location; // 问题位置
    }

    /**
     * AI 优化建议
     */
    @Data
    @Builder
    public static class AiSuggestion {
        private String title;
        private String description;
        private String priority; // high / medium / low
        private String example;
        private String expectedImprovement;
    }
}