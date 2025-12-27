package com.dbdev.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * DB Dev 配置属性
 */
@Data
@ConfigurationProperties(prefix = "db.dev")
public class DbDevProperties {
    
    /**
     * 是否启用
     */
    private boolean enabled = false;
    
    /**
     * UI 访问路径
     */
    private String uiPath = "/db-dev";
    
    /**
     * SQL 配置
     */
    private SqlConfig sql = new SqlConfig();
    
    /**
     * 安全配置
     */
    private SecurityConfig security = new SecurityConfig();
    
    @Data
    public static class SqlConfig {
        /**
         * 只读模式
         */
        private boolean readOnly = true;
        
        /**
         * 最大返回行数
         */
        private int maxRows = 500;
        
        /**
         * 超时时间（秒）
         */
        private int timeout = 30;
        
        /**
         * 允许的 SQL 关键字
         */
        private List<String> allowedKeywords = new ArrayList<>(List.of("SELECT", "SHOW", "DESCRIBE"));
    }
    
    @Data
    public static class SecurityConfig {
        /**
         * 是否启用安全认证
         */
        private boolean enabled = true;
        
        /**
         * 用户名
         */
        private String username = "admin";
        
        /**
         * 密码
         */
        private String password = "admin";
        
        /**
         * IP 白名单
         */
        private List<String> allowedIps = new ArrayList<>(List.of("127.0.0.1", "localhost"));
    }
}
