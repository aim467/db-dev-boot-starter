package com.dbdev.core.model;

import lombok.Builder;
import lombok.Data;

/**
 * 连接池统计信息
 */
@Data
@Builder
public class PoolStats {
    
    /**
     * 数据源名称
     */
    private String dataSourceName;
    
    /**
     * 连接池类型
     */
    private String poolType;
    
    /**
     * 活跃连接数
     */
    private int activeConnections;
    
    /**
     * 空闲连接数
     */
    private int idleConnections;
    
    /**
     * 总连接数
     */
    private int totalConnections;
    
    /**
     * 最大连接数
     */
    private int maxConnections;
    
    /**
     * 最小空闲连接数
     */
    private int minIdle;
    
    /**
     * 等待连接的线程数
     */
    private int waitingThreads;
    
    /**
     * 连接使用率 (0-100)
     */
    private double usagePercent;
    
    /**
     * 累计创建连接数（部分连接池支持）
     */
    private Long createCount;
    
    /**
     * 累计销毁连接数（部分连接池支持）
     */
    private Long destroyCount;
    
    /**
     * 连接超时时间（毫秒）
     */
    private Long connectionTimeout;
    
    /**
     * 空闲超时时间（毫秒）
     */
    private Long idleTimeout;
    
    /**
     * 连接最大生命周期（毫秒）
     */
    private Long maxLifetime;
}
