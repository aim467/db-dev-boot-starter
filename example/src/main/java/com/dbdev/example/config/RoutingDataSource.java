package com.dbdev.example.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {
    
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    
    public static void setDataSource(String dataSource) {
        contextHolder.set(dataSource);
    }
    
    public static void clearDataSource() {
        contextHolder.remove();
    }
    
    @Override
    protected Object determineCurrentLookupKey() {
        return contextHolder.get();
    }
}