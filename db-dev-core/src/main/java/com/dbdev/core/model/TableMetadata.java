package com.dbdev.core.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 表元数据
 */
@Data
@Builder
public class TableMetadata {
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表类型
     */
    private String tableType;
    
    /**
     * 表注释
     */
    private String remarks;
    
    /**
     * 字段列表
     */
    private List<ColumnMetadata> columns;
    
    /**
     * 索引列表
     */
    private List<IndexMetadata> indexes;
    
    /**
     * 主键列表
     */
    private List<String> primaryKeys;
}
