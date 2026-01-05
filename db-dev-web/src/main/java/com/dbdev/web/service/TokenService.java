package com.dbdev.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Token 管理服务（内存存储）
 */
@Slf4j
@Service
public class TokenService {
    
    /**
     * Token存储：token -> username
     */
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    
    /**
     * Token过期时间：token -> 过期时间戳
     */
    private final Map<String, Long> tokenExpiry = new ConcurrentHashMap<>();
    
    /**
     * Token默认过期时间：2小时
     */
    private static final long DEFAULT_EXPIRY_TIME = 2 * 60 * 60 * 1000; // 2小时
    
    /**
     * 定时清理任务
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public TokenService() {
        // 每5分钟清理一次过期token
        scheduler.scheduleAtFixedRate(this::cleanExpiredTokens, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * 生成Token
     */
    public String generateToken(String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, username);
        tokenExpiry.put(token, System.currentTimeMillis() + DEFAULT_EXPIRY_TIME);
        log.debug("Generated token for user: {}", username);
        return token;
    }
    
    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        String username = tokenStore.get(token);
        if (username == null) {
            return false;
        }
        
        Long expiry = tokenExpiry.get(token);
        if (expiry == null || expiry < System.currentTimeMillis()) {
            // Token已过期，移除
            removeToken(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * 根据Token获取用户名
     */
    public String getUsername(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return tokenStore.get(token);
    }
    
    /**
     * 移除Token
     */
    public void removeToken(String token) {
        tokenStore.remove(token);
        tokenExpiry.remove(token);
        log.debug("Removed token: {}", token);
    }
    
    /**
     * 刷新Token过期时间
     */
    public void refreshToken(String token) {
        if (tokenStore.containsKey(token)) {
            tokenExpiry.put(token, System.currentTimeMillis() + DEFAULT_EXPIRY_TIME);
        }
    }
    
    /**
     * 清理过期Token
     */
    private void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenExpiry.entrySet().removeIf(entry -> {
            if (entry.getValue() < now) {
                String token = entry.getKey();
                tokenStore.remove(token);
                log.debug("Cleaned expired token: {}", token);
                return true;
            }
            return false;
        });
    }
    
    /**
     * 关闭服务时清理资源
     */
    public void shutdown() {
        scheduler.shutdown();
        tokenStore.clear();
        tokenExpiry.clear();
    }
}

