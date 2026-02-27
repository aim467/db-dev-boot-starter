package com.dbdev.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI配置属性
 */
@Data
@ConfigurationProperties(prefix = "db.dev.ai")
@Component
public class AiProperties {

    /**
     * 是否启用AI功能
     */
    private boolean enabled = false;

    /**
     * AI提供商: modelscope, deepseek
     */
    private String provider = "modelscope";

    /**
     * ModelScope配置
     */
    private ModelScopeConfig modelscope = new ModelScopeConfig();

    /**
     * DeepSeek配置
     */
    private DeepSeekConfig deepseek = new DeepSeekConfig();

    /**
     * 请求超时时间（秒）
     */
    private int timeout = 30;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    @Data
    public static class ModelScopeConfig {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 模型名称
         */
        private String model = "qwen-turbo";

        /**
         * API端点
         */
        private String endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        /**
         * 温度参数 (0-2)
         */
        private double temperature = 0.7;

        /**
         * 最大token数
         */
        private int maxTokens = 2000;
    }

    @Data
    public static class DeepSeekConfig {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 模型名称
         */
        private String model = "deepseek-chat";

        /**
         * API端点
         */
        private String endpoint = "https://api.deepseek.com/v1/chat/completions";

        /**
         * 温度参数 (0-2)
         */
        private double temperature = 0.7;

        /**
         * 最大token数
         */
        private int maxTokens = 2000;
    }
}
