package com.dbdev.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db.dev.ai")
public class AiProperties {
    private boolean enabled;
    private String openaiApiKey;
    private String openaiModel = "gpt-3.5-turbo";
    private String openaiEndpoint = "https://api.openai.com/v1/chat/completions";
    private int timeout = 30;

    // Getters and setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getOpenaiApiKey() {
        return openaiApiKey;
    }

    public void setOpenaiApiKey(String openaiApiKey) {
        this.openaiApiKey = openaiApiKey;
    }

    public String getOpenaiModel() {
        return openaiModel;
    }

    public void setOpenaiModel(String openaiModel) {
        this.openaiModel = openaiModel;
    }

    public String getOpenaiEndpoint() {
        return openaiEndpoint;
    }

    public void setOpenaiEndpoint(String openaiEndpoint) {
        this.openaiEndpoint = openaiEndpoint;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}