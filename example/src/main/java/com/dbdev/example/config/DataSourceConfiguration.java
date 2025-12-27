package com.dbdev.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfiguration {
    
    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;
    
    @Autowired
    @Qualifier("h2DataSource")
    private DataSource h2DataSource;
    
    @Primary
    @Bean("dataSource")
    public DataSource dataSource() {
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