package com.dbdev.core.service;

import com.dbdev.core.model.PoolStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * 连接池统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PoolStatsService {

    private final ApplicationContext applicationContext;

    /**
     * 获取指定数据源的连接池统计信息
     */
    public PoolStats getPoolStats(String dataSourceName) {
        try {
            DataSource dataSource = applicationContext.getBean(dataSourceName, DataSource.class);
            return extractPoolStats(dataSourceName, dataSource);
        } catch (Exception e) {
            log.error("获取连接池统计信息失败: {}", dataSourceName, e);
            throw new RuntimeException("获取连接池统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 提取连接池统计信息
     */
    private PoolStats extractPoolStats(String name, DataSource dataSource) {
        String className = dataSource.getClass().getName();
        
        if (className.contains("HikariDataSource")) {
            return extractHikariStats(name, dataSource);
        } else if (className.contains("DruidDataSource")) {
            return extractDruidStats(name, dataSource);
        } else if (className.contains("TomcatDataSource") || className.contains("org.apache.tomcat")) {
            return extractTomcatStats(name, dataSource);
        } else if (className.contains("DBCP") || className.contains("BasicDataSource")) {
            return extractDbcpStats(name, dataSource);
        } else {
            return buildUnknownPoolStats(name, dataSource);
        }
    }

    /**
     * 提取 HikariCP 统计信息
     */
    private PoolStats extractHikariStats(String name, DataSource dataSource) {
        try {
            // 获取 HikariPoolMXBean
            Method getHikariPoolMXBean = dataSource.getClass().getMethod("getHikariPoolMXBean");
            Object poolMXBean = getHikariPoolMXBean.invoke(dataSource);
            
            // 获取配置信息
            Method getMaxPoolSize = dataSource.getClass().getMethod("getMaximumPoolSize");
            Method getMinIdle = dataSource.getClass().getMethod("getMinimumIdle");
            Method getConnTimeout = dataSource.getClass().getMethod("getConnectionTimeout");
            Method getIdleTimeout = dataSource.getClass().getMethod("getIdleTimeout");
            Method getMaxLifetime = dataSource.getClass().getMethod("getMaxLifetime");
            
            int maxPoolSize = (int) getMaxPoolSize.invoke(dataSource);
            int minIdle = (int) getMinIdle.invoke(dataSource);
            long connTimeout = (long) getConnTimeout.invoke(dataSource);
            long idleTimeout = (long) getIdleTimeout.invoke(dataSource);
            long maxLifetime = (long) getMaxLifetime.invoke(dataSource);
            
            int activeConnections = 0;
            int idleConnections = 0;
            int totalConnections = 0;
            int waitingThreads = 0;
            
            if (poolMXBean != null) {
                Method getActiveConnections = poolMXBean.getClass().getMethod("getActiveConnections");
                Method getIdleConnections = poolMXBean.getClass().getMethod("getIdleConnections");
                Method getTotalConnections = poolMXBean.getClass().getMethod("getTotalConnections");
                Method getThreadsAwaitingConnection = poolMXBean.getClass().getMethod("getThreadsAwaitingConnection");
                
                activeConnections = (int) getActiveConnections.invoke(poolMXBean);
                idleConnections = (int) getIdleConnections.invoke(poolMXBean);
                totalConnections = (int) getTotalConnections.invoke(poolMXBean);
                waitingThreads = (int) getThreadsAwaitingConnection.invoke(poolMXBean);
            }
            
            double usagePercent = maxPoolSize > 0 ? (activeConnections * 100.0 / maxPoolSize) : 0;
            
            return PoolStats.builder()
                    .dataSourceName(name)
                    .poolType("HikariCP")
                    .activeConnections(activeConnections)
                    .idleConnections(idleConnections)
                    .totalConnections(totalConnections)
                    .maxConnections(maxPoolSize)
                    .minIdle(minIdle)
                    .waitingThreads(waitingThreads)
                    .usagePercent(Math.round(usagePercent * 100.0) / 100.0)
                    .connectionTimeout(connTimeout)
                    .idleTimeout(idleTimeout)
                    .maxLifetime(maxLifetime)
                    .build();
                    
        } catch (Exception e) {
            log.warn("提取 HikariCP 统计信息失败: {}", e.getMessage());
            return buildUnknownPoolStats(name, dataSource);
        }
    }

    /**
     * 提取 Druid 统计信息
     */
    private PoolStats extractDruidStats(String name, DataSource dataSource) {
        try {
            Method getActiveCount = dataSource.getClass().getMethod("getActiveCount");
            Method getPoolingCount = dataSource.getClass().getMethod("getPoolingCount");
            Method getMaxActive = dataSource.getClass().getMethod("getMaxActive");
            Method getMinIdle = dataSource.getClass().getMethod("getMinIdle");
            Method getWaitThreadCount = dataSource.getClass().getMethod("getWaitThreadCount");
            Method getConnectCount = dataSource.getClass().getMethod("getConnectCount");
            Method getDestroyCount = dataSource.getClass().getMethod("getDestroyCount");
            Method getMaxWait = dataSource.getClass().getMethod("getMaxWait");
            Method getMinEvictableIdleTimeMillis = dataSource.getClass().getMethod("getMinEvictableIdleTimeMillis");
            
            int activeCount = (int) getActiveCount.invoke(dataSource);
            int poolingCount = (int) getPoolingCount.invoke(dataSource);
            int maxActive = (int) getMaxActive.invoke(dataSource);
            int minIdle = (int) getMinIdle.invoke(dataSource);
            int waitThreadCount = (int) getWaitThreadCount.invoke(dataSource);
            long connectCount = (long) getConnectCount.invoke(dataSource);
            long destroyCount = (long) getDestroyCount.invoke(dataSource);
            long maxWait = (long) getMaxWait.invoke(dataSource);
            long minEvictableIdleTime = (long) getMinEvictableIdleTimeMillis.invoke(dataSource);
            
            int totalConnections = activeCount + poolingCount;
            double usagePercent = maxActive > 0 ? (activeCount * 100.0 / maxActive) : 0;
            
            return PoolStats.builder()
                    .dataSourceName(name)
                    .poolType("Druid")
                    .activeConnections(activeCount)
                    .idleConnections(poolingCount)
                    .totalConnections(totalConnections)
                    .maxConnections(maxActive)
                    .minIdle(minIdle)
                    .waitingThreads(waitThreadCount)
                    .usagePercent(Math.round(usagePercent * 100.0) / 100.0)
                    .createCount(connectCount)
                    .destroyCount(destroyCount)
                    .connectionTimeout(maxWait)
                    .idleTimeout(minEvictableIdleTime)
                    .build();
                    
        } catch (Exception e) {
            log.warn("提取 Druid 统计信息失败: {}", e.getMessage());
            return buildUnknownPoolStats(name, dataSource);
        }
    }

    /**
     * 提取 Tomcat JDBC Pool 统计信息
     */
    private PoolStats extractTomcatStats(String name, DataSource dataSource) {
        try {
            Method getActive = dataSource.getClass().getMethod("getActive");
            Method getIdle = dataSource.getClass().getMethod("getIdle");
            Method getSize = dataSource.getClass().getMethod("getSize");
            Method getMaxActive = dataSource.getClass().getMethod("getMaxActive");
            Method getMinIdle = dataSource.getClass().getMethod("getMinIdle");
            Method getWaitCount = dataSource.getClass().getMethod("getWaitCount");
            
            int active = (int) getActive.invoke(dataSource);
            int idle = (int) getIdle.invoke(dataSource);
            int size = (int) getSize.invoke(dataSource);
            int maxActive = (int) getMaxActive.invoke(dataSource);
            int minIdle = (int) getMinIdle.invoke(dataSource);
            int waitCount = (int) getWaitCount.invoke(dataSource);
            
            double usagePercent = maxActive > 0 ? (active * 100.0 / maxActive) : 0;
            
            return PoolStats.builder()
                    .dataSourceName(name)
                    .poolType("Tomcat JDBC")
                    .activeConnections(active)
                    .idleConnections(idle)
                    .totalConnections(size)
                    .maxConnections(maxActive)
                    .minIdle(minIdle)
                    .waitingThreads(waitCount)
                    .usagePercent(Math.round(usagePercent * 100.0) / 100.0)
                    .build();
                    
        } catch (Exception e) {
            log.warn("提取 Tomcat JDBC Pool 统计信息失败: {}", e.getMessage());
            return buildUnknownPoolStats(name, dataSource);
        }
    }

    /**
     * 提取 DBCP 统计信息
     */
    private PoolStats extractDbcpStats(String name, DataSource dataSource) {
        try {
            Method getNumActive = dataSource.getClass().getMethod("getNumActive");
            Method getNumIdle = dataSource.getClass().getMethod("getNumIdle");
            Method getMaxTotal = dataSource.getClass().getMethod("getMaxTotal");
            Method getMinIdle = dataSource.getClass().getMethod("getMinIdle");
            
            int numActive = (int) getNumActive.invoke(dataSource);
            int numIdle = (int) getNumIdle.invoke(dataSource);
            int maxTotal = (int) getMaxTotal.invoke(dataSource);
            int minIdle = (int) getMinIdle.invoke(dataSource);
            
            double usagePercent = maxTotal > 0 ? (numActive * 100.0 / maxTotal) : 0;
            
            return PoolStats.builder()
                    .dataSourceName(name)
                    .poolType("DBCP")
                    .activeConnections(numActive)
                    .idleConnections(numIdle)
                    .totalConnections(numActive + numIdle)
                    .maxConnections(maxTotal)
                    .minIdle(minIdle)
                    .waitingThreads(0)
                    .usagePercent(Math.round(usagePercent * 100.0) / 100.0)
                    .build();
                    
        } catch (Exception e) {
            log.warn("提取 DBCP 统计信息失败: {}", e.getMessage());
            return buildUnknownPoolStats(name, dataSource);
        }
    }

    /**
     * 构建未知连接池类型的统计信息
     */
    private PoolStats buildUnknownPoolStats(String name, DataSource dataSource) {
        return PoolStats.builder()
                .dataSourceName(name)
                .poolType(dataSource.getClass().getSimpleName())
                .activeConnections(-1)
                .idleConnections(-1)
                .totalConnections(-1)
                .maxConnections(-1)
                .minIdle(-1)
                .waitingThreads(-1)
                .usagePercent(-1)
                .build();
    }
}
