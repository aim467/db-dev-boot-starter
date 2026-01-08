package com.dbdev.core.model;

import lombok.Builder;
import lombok.Data;

/**
 * 字段元数据
 */
@Data
@Builder
public class ColumnMetadata {

    /**
     * 字段名
     */
    private String columnName;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 字段大小
     */
    private Integer columnSize;

    /**
     * 小数位数
     */
    private Integer decimalDigits;

    /**
     * 是否可为空
     */
    private boolean nullable;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 字段注释
     */
    private String remarks;

    /**
     * 是否自增
     */
    private boolean autoIncrement;

    /**
     * 是否主键
     */
    private boolean primaryKey;

    /**
     * Java 数据类型
     */
    private String javaType;

    /**
     * 格式化后的类型（用于模板渲染）
     */
    private String formattedType;

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setFormattedType(String formattedType) {
        this.formattedType = formattedType;
    }
}
