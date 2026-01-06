package com.dbdev.core.druid;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;

/**
 * Druid 数据源信息提供者
 */
@Slf4j
@Component
public class DruidDataSourceProvider {

    private final DataSource dataSource;

    public DruidDataSourceProvider(DataSource dataSource) {
        this.dataSource = dataSource;
        log.info("DruidDataSourceProvider initialized with datasource: {}",
                dataSource.getClass().getSimpleName());
    }

    /**
     * 检查是否为 Druid 数据源
     */
    public boolean isDruidDataSource() {
        return dataSource instanceof DruidDataSource;
    }

    /**
     * 获取 Druid 连接池状态信息
     */
    public Map<String, Object> getPoolStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        if (dataSource instanceof DruidDataSource druid) {
            // 基本信息
            stats.put("name", druid.getName());
            stats.put("dbType", druid.getDbType());
            stats.put("driverClassName", druid.getDriverClassName());
            stats.put("url", maskUrl(druid.getUrl()));

            // 连接池配置
            stats.put("initialSize", druid.getInitialSize());
            stats.put("minIdle", druid.getMinIdle());
            stats.put("maxActive", druid.getMaxActive());
            stats.put("maxWait", druid.getMaxWait());

            // 实时状态
            stats.put("activeCount", druid.getActiveCount());
            stats.put("poolingCount", druid.getPoolingCount());
            stats.put("waitThreadCount", druid.getWaitThreadCount());

            // 统计信息
            stats.put("connectCount", druid.getConnectCount());
            stats.put("closeCount", druid.getCloseCount());
            stats.put("createCount", druid.getCreateCount());
            stats.put("destroyCount", druid.getDestroyCount());
            stats.put("connectErrorCount", druid.getConnectErrorCount());

            // 计算使用率
            int maxActive = druid.getMaxActive();
            int activeCount = druid.getActiveCount();
            double usageRate = maxActive > 0 ? (double) activeCount / maxActive * 100 : 0;
            stats.put("usageRate", Math.round(usageRate * 100) / 100.0);
        }
        return stats;
    }


    /**
     * 获取 SQL 统计信息
     */
    public List<Map<String, Object>> getSqlStats() {
        List<Map<String, Object>> sqlStatsList = new ArrayList<>();
        if (dataSource instanceof DruidDataSource druid) {
            try {
                var sqlStatMap = druid.getDataSourceStat().getSqlStatMap();
                if (sqlStatMap != null) {
                    sqlStatMap.values().forEach(sqlStat -> {
                        Map<String, Object> stat = new LinkedHashMap<>();
                        stat.put("sql", truncateSql(sqlStat.getSql()));
                        stat.put("executeCount", sqlStat.getExecuteCount());
                        stat.put("totalTime", sqlStat.getExecuteMillisTotal());
                        stat.put("maxTime", sqlStat.getExecuteMillisMax());
                        stat.put("avgTime", sqlStat.getExecuteCount() > 0
                                ? sqlStat.getExecuteMillisTotal() / sqlStat.getExecuteCount() : 0);
                        stat.put("errorCount", sqlStat.getErrorCount());
                        stat.put("runningCount", sqlStat.getRunningCount());
                        stat.put("concurrentMax", sqlStat.getConcurrentMax());
                        stat.put("fetchRowCount", sqlStat.getFetchRowCount());
                        stat.put("updateCount", sqlStat.getUpdateCount());
                        sqlStatsList.add(stat);
                    });
                }
            } catch (Exception e) {
                log.warn("Failed to get SQL stats: {}", e.getMessage());
            }
        }
        // 按执行次数降序排序
        sqlStatsList.sort((a, b) -> Long.compare(
                (Long) b.get("executeCount"), (Long) a.get("executeCount")));
        return sqlStatsList;
    }

    /**
     * 获取 Druid 数据源
     */
    public DruidDataSource getDruidDataSource() {
        if (dataSource instanceof DruidDataSource druid) {
            return druid;
        }
        return null;
    }

    /**
     * 重置统计信息
     */
    public void resetStats() {
        if (dataSource instanceof DruidDataSource druid) {
            druid.resetStat();
            log.info("Druid stats reset");
        }
    }

    /**
     * 掩码 URL 中的敏感信息
     */
    private String maskUrl(String url) {
        if (url == null) return null;
        // 掩码密码参数
        return url.replaceAll("password=[^&]*", "password=***");
    }

    /**
     * 截断过长的 SQL
     */
    private String truncateSql(String sql) {
        if (sql == null) return null;
        if (sql.length() > 200) {
            return sql.substring(0, 200) + "...";
        }
        return sql;
    }
}