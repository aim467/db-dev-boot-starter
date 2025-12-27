package com.dbdev.core.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 数据库元数据
 */
@Data
@Builder
public class DatabaseMetadata {
    
    /**
     * 数据库名称
     */
    private String databaseName;
    
    /**
     * 数据库产品名称
     */
    private String productName;
    
    /**
     * 数据库版本
     */
    private String productVersion;
    
    /**
     * 表列表
     */
    private List<TableMetadata> tables;
}
