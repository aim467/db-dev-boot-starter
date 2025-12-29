package ${entityPackage};

<#if config.entity.useLombok>
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
</#if>
<#if config.entity.useJpa>
import javax.persistence.*;
</#if>

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * ${table.remarks!table.tableName}
 * 
 * @author ${author}
 * @date ${date}
 */
<#if config.entity.useLombok>
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
</#if>
<#if config.entity.useJpa>
@Entity
@Table(name = "${table.tableName}")
</#if>
public class ${className} <#if config.entity.superClass??>extends ${config.entity.superClass}</#if> {

<#list table.columns as column>
    <#-- 字段注释 -->
    /**
     * ${column.remarks!column.columnName}
     */
    <#-- JPA 注解 -->
    <#if config.entity.useJpa>
        <#if primaryKeys?seq_contains(column.columnName)>
    @Id
        </#if>
        <#if column.autoIncrement>
    @GeneratedValue(strategy = GenerationType.IDENTITY)
        </#if>
    @Column(name = "${column.columnName}"<#if column.columnName != column.columnName?lower_case>, columnDefinition = "${column.typeName}"</#if>)
    </#if>
    <#-- 字段定义 -->
    private ${getJavaType(column.typeName)} ${underscoreToCamelCase(column.columnName, false)};

</#list>
<#if !config.entity.useLombok>
    <#-- 如果没有使用 Lombok，生成 getter/setter -->
    <#list table.columns as column>
    public ${getJavaType(column.typeName)} get${underscoreToCamelCase(column.columnName, true)}() {
        return this.${underscoreToCamelCase(column.columnName, false)};
    }
    
    public void set${underscoreToCamelCase(column.columnName, true)}(${getJavaType(column.typeName)} ${underscoreToCamelCase(column.columnName, false)}) {
        this.${underscoreToCamelCase(column.columnName, false)} = ${underscoreToCamelCase(column.columnName, false)};
    }
    
    </#list>
</#if>
}

<#-- FreeMarker 函数定义 -->
<#function getJavaType dbType>
    <#if dbType?contains("VARCHAR") || dbType?contains("CHAR") || dbType?contains("TEXT")>
        <#return "String">
    <#elseif dbType?contains("INT") || dbType?contains("TINYINT") || dbType?contains("SMALLINT")>
        <#return "Integer">
    <#elseif dbType?contains("BIGINT")>
        <#return "Long">
    <#elseif dbType?contains("DECIMAL") || dbType?contains("NUMERIC")>
        <#return "BigDecimal">
    <#elseif dbType?contains("DOUBLE") || dbType?contains("FLOAT")>
        <#return "Double">
    <#elseif dbType?contains("DATETIME") || dbType?contains("TIMESTAMP")>
        <#return "LocalDateTime">
    <#elseif dbType?contains("DATE")>
        <#return "LocalDate">
    <#elseif dbType?contains("BOOLEAN") || dbType?contains("BIT")>
        <#return "Boolean">
    <#else>
        <#return "String">
    </#if>
</#function>

<#function underscoreToCamelCase underscore firstCharUpper=false>
    <#if !underscore?? || underscore == "">
        <#return underscore>
    </#if>
    <#assign parts = underscore?split("_")>
    <#assign result = "">
    <#list parts as part>
        <#if part != "">
            <#if part_index == 0 && !firstCharUpper>
                <#assign result = result + part?lower_case>
            <#else>
                <#assign result = result + part?cap_first>
            </#if>
        </#if>
    </#list>
    <#return result>
</#function>