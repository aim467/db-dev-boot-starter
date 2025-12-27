package com.dbdev.core.service;

import com.dbdev.core.model.DataSourceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据源管理服务
 */
@Slf4j
@Service
public class DataSourceService {

    private final ApplicationContext applicationContext;

    public DataSourceService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取所有数据源
     */
    public List<DataSourceInfo> getAllDataSources() {
        Map<String, DataSource> dataSourceBeans = applicationContext.getBeansOfType(DataSource.class);
        List<DataSourceInfo> dataSourceInfos = new ArrayList<>();
        Set<String> processedDataSources = new HashSet<>();

        dataSourceBeans.forEach((name, dataSource) -> {
            try {
                // 跳过已处理的数据源（避免重复）
                if (processedDataSources.contains(name)) {
                    return;
                }

                // 跳过代理和包装类
                if (shouldSkipDataSource(dataSource)) {
                    log.debug("Skipping datasource: {} (type: {})", name, dataSource.getClass().getName());
                    return;
                }

                // 处理路由数据源（多数据源场景）
                if (dataSource instanceof AbstractRoutingDataSource) {
                    log.debug("Processing routing datasource: {} (type: {})", name, dataSource.getClass().getName());
                    return;
                }

                // 处理普通数据源
                DataSourceInfo info = extractDataSourceInfo(name, dataSource);
                dataSourceInfos.add(info);
                processedDataSources.add(name);

            } catch (Exception e) {
                log.error("Failed to extract datasource info for: {}", name, e);
            }
        });

        return dataSourceInfos;
    }

    /**
     * 判断是否应该跳过该数据源
     */
    private boolean shouldSkipDataSource(DataSource dataSource) {
        String className = dataSource.getClass().getName();

        // 跳过代理类
        if (className.contains("Proxy") || className.contains("$")) {
            return true;
        }

        // 跳过 Spring 内部包装类
        if (className.startsWith("org.springframework.jdbc.datasource.")
                && !className.contains("DriverManagerDataSource")
                && !className.contains("SimpleDriverDataSource")) {
            return true;
        }

        // 跳过特定的包装类型
        List<String> skipTypes = Arrays.asList(
                "LazyConnectionDataSourceProxy",
                "TransactionAwareDataSourceProxy",
                "DelegatingDataSource"
        );

        return skipTypes.stream().anyMatch(className::contains);
    }

    /**
     * 提取路由数据源信息（多数据源场景）
     */
    @Deprecated
    private List<DataSourceInfo> extractRoutingDataSourceInfo(String routingName, AbstractRoutingDataSource routingDataSource) {
        List<DataSourceInfo> infos = new ArrayList<>();

        try {
            // 通过反射获取目标数据源映射
            Field resolvedDataSourcesField = AbstractRoutingDataSource.class.getDeclaredField("resolvedDataSources");
            resolvedDataSourcesField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<Object, DataSource> resolvedDataSources = (Map<Object, DataSource>) resolvedDataSourcesField.get(routingDataSource);

            if (resolvedDataSources != null) {
                resolvedDataSources.forEach((key, dataSource) -> {
                    try {
                        String dsName = routingName + "." + key.toString();
                        DataSourceInfo info = extractDataSourceInfo(dsName, dataSource);
                        // 标记第一个为主数据源
                        if (infos.isEmpty()) {
                            info.setPrimary(true);
                        }
                        infos.add(info);
                    } catch (Exception e) {
                        log.error("Failed to extract routing datasource info for key: {}", key, e);
                    }
                });
            }

            // 如果没有找到任何数据源，尝试直接连接路由数据源
            if (infos.isEmpty()) {
                DataSourceInfo info = extractDataSourceInfo(routingName, routingDataSource);
                info.setPrimary(true);
                infos.add(info);
            }

        } catch (Exception e) {
            log.warn("Failed to extract routing datasource details for: {}, falling back to direct connection", routingName, e);
            try {
                DataSourceInfo info = extractDataSourceInfo(routingName, routingDataSource);
                info.setPrimary(true);
                infos.add(info);
            } catch (Exception ex) {
                log.error("Failed to extract datasource info for: {}", routingName, ex);
            }
        }

        return infos;
    }

    /**
     * 提取数据源信息
     */
    private DataSourceInfo extractDataSourceInfo(String name, DataSource dataSource) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            return DataSourceInfo.builder()
                    .name(name)
                    .type(dataSource.getClass().getSimpleName())
                    .url(maskSensitiveInfo(metaData.getURL()))
                    .username(maskUsername(metaData.getUserName()))
                    .databaseType(metaData.getDatabaseProductName())
                    .primary("dataSource".equals(name))
                    .build();
        }
    }

    /**
     * 脱敏 URL 中的敏感信息
     */
    private String maskSensitiveInfo(String url) {
        if (url == null) return null;
        // 移除密码参数
        return url.replaceAll("password=[^&]*", "password=***");
    }

    /**
     * 脱敏用户名
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) return "***";
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }

    /**
     * 根据名称获取数据源
     */
    public DataSource getDataSource(String name) {
        return applicationContext.getBean(name, DataSource.class);
    }
}
