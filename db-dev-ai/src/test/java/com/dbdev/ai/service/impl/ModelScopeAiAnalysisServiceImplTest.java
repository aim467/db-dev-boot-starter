package com.dbdev.ai.service.impl;

import com.dbdev.ai.config.AiProperties;
import com.dbdev.ai.model.AiAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ModelScope AI分析服务测试
 */
class ModelScopeAiAnalysisServiceImplTest {

    private ModelScopeAiAnalysisServiceImpl aiAnalysisService;
    private AiProperties aiProperties;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.setEnabled(true);
        aiProperties.getModelscope().setApiKey("test-api-key");
        aiProperties.getModelscope().setModel("qwen-turbo");
        
        aiAnalysisService = new ModelScopeAiAnalysisServiceImpl(aiProperties);
    }

    @Test
    void testIsEnabled_WithApiKey() {
        assertTrue(aiAnalysisService.isEnabled());
    }

    @Test
    void testIsEnabled_WithoutApiKey() {
        aiProperties.getModelscope().setApiKey(null);
        ModelScopeAiAnalysisServiceImpl service = new ModelScopeAiAnalysisServiceImpl(aiProperties);
        assertFalse(service.isEnabled());
    }

    @Test
    void testIsEnabled_DisabledGlobally() {
        aiProperties.setEnabled(false);
        ModelScopeAiAnalysisServiceImpl service = new ModelScopeAiAnalysisServiceImpl(aiProperties);
        assertFalse(service.isEnabled());
    }

    @Test
    void testAnalyzeSql_Disabled() {
        aiProperties.setEnabled(false);
        ModelScopeAiAnalysisServiceImpl service = new ModelScopeAiAnalysisServiceImpl(aiProperties);
        
        AiAnalysisResult result = service.analyzeSql("SELECT * FROM users", "MySQL");
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("未启用"));
    }

    @Test
    void testAnalyzeExplain_Disabled() {
        aiProperties.setEnabled(false);
        ModelScopeAiAnalysisServiceImpl service = new ModelScopeAiAnalysisServiceImpl(aiProperties);
        
        Map<String, Object> explainData = new HashMap<>();
        explainData.put("type", "ALL");
        explainData.put("rows", 10000);
        
        AiAnalysisResult result = service.analyzeExplain(explainData, "MySQL");
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testConfiguration() {
        assertEquals("qwen-turbo", aiProperties.getModelscope().getModel());
        assertEquals(0.7, aiProperties.getModelscope().getTemperature());
        assertEquals(2000, aiProperties.getModelscope().getMaxTokens());
        assertEquals(30, aiProperties.getTimeout());
        assertEquals(3, aiProperties.getMaxRetries());
    }
}
