package com.dbdev.ai.config;

import com.dbdev.ai.service.AiAnalysisService;
import com.dbdev.ai.service.impl.DashScopeAiAnalysisServiceImpl;
import com.dbdev.ai.service.impl.DeepSeekAiAnalysisServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI模块自动配置
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@Slf4j
@ConditionalOnProperty(prefix = "db.dev.ai", name = "enabled", havingValue = "true")
public class AiAutoConfiguration {

    @Bean
    public AiAnalysisService aiAnalysisService(AiProperties aiProperties) {
        log.info("Initializing AI analysis service...");
        String provider = aiProperties.getProvider();
        
        if ("deepseek".equalsIgnoreCase(provider)) {
            return new DeepSeekAiAnalysisServiceImpl(aiProperties);
        }
        
        // 默认使用 DashScope (DashScope)
        return new DashScopeAiAnalysisServiceImpl(aiProperties);
    }
}
