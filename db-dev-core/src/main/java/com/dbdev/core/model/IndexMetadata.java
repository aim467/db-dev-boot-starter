package com.dbdev.core.model;

import lombok.Builder;
import lombok.Data;

/**
 * 索引元数据
 */
@Data
@Builder
public class IndexMetadata {
    
    /**
     * 索引名称
     */
    private String indexName;
    
    /**
     * 字段名
     */
    private String columnName;
    
    /**
     * 是否唯一索引
     */
    private boolean unique;
    
    /**
     * 索引类型
     */
    private String type;
}
