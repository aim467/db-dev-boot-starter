package com.dbdev.ai.config;

import com.dbdev.ai.service.impl.LocalAnalysisService;
import com.dbdev.ai.service.impl.OpenAiAnalysisService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "db.dev.ai", name = "enabled", havingValue = "true")
    public OpenAiAnalysisService openAiAnalysisService(AiProperties aiProperties) {
        return new OpenAiAnalysisService(aiProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "db.dev.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LocalAnalysisService localAnalysisService(AiProperties aiProperties) {
        return new LocalAnalysisService(aiProperties);
    }
}