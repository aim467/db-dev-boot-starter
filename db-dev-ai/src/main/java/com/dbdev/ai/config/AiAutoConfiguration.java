package com.dbdev.ai.config;

import com.dbdev.ai.service.AiAnalysisService;
import com.dbdev.ai.service.impl.ModelScopeAiAnalysisServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI模块自动配置
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@ConditionalOnProperty(prefix = "db.dev.ai", name = "enabled", havingValue = "true")
public class AiAutoConfiguration {

    @Bean
    public AiAnalysisService aiAnalysisService(AiProperties aiProperties) {
        return new ModelScopeAiAnalysisServiceImpl(aiProperties);
    }
}
