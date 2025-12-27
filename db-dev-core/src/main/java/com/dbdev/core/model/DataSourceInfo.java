package com.dbdev.core.model;

import lombok.Builder;
import lombok.Data;

/**
 * 数据源信息
 */
@Data
@Builder
public class DataSourceInfo {
    
    /**
     * 数据源名称
     */
    private String name;
    
    /**
     * 数据源类型
     */
    private String type;
    
    /**
     * JDBC URL
     */
    private String url;
    
    /**
     * 用户名（脱敏）
     */
    private String username;
    
    /**
     * 数据库类型
     */
    private String databaseType;
    
    /**
     * 是否为主数据源
     */
    private boolean primary;
}
