package com.dbdev.example.config;

import com.dbdev.example.config.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultiDataSourceConfig {

    // 配置主数据源 - MySQL
    @Primary
    @Bean("mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    // 配置次数据源 - H2
    @Bean("h2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.h2")
    public DataSource h2DataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    // 配置路由数据源
    @Primary
    @Bean("routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("mysqlDataSource") DataSource mysqlDataSource,
            @Qualifier("h2DataSource") DataSource h2DataSource) {
        
        RoutingDataSource routingDataSource = new RoutingDataSource();
        
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("mysql", mysqlDataSource);
        dataSourceMap.put("h2", h2DataSource);
        
        // 设置默认数据源
        routingDataSource.setDefaultTargetDataSource(mysqlDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);
        
        return routingDataSource;
    }
}