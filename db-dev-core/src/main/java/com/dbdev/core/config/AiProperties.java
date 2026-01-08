package com.dbdev.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 功能配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "db.dev.ai")
public class AiProperties {

    /**
     * 是否启用 AI 功能
     */
    private boolean enabled = false;

    /**
     * AI 提供商: openai / local / azure
     */
    private String provider = "openai";

    /**
     * API Key (建议通过环境变量 DB_DEV_AI_API_KEY 设置)
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model = "gpt-3.5-turbo";

    /**
     * API 基础地址
     */
    private String baseUrl;

    /**
     * 请求超时时间（秒）
     */
    private int timeout = 30;

    /**
     * Temperature 参数 (0-1)
     */
    private double temperature = 0.1;

    /**
     * 最大 token 数
     */
    private int maxTokens = 2000;

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 缓存过期时间（分钟）
     */
    private int cacheExpiry = 60;
}