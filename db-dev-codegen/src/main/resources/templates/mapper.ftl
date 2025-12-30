package ${mapperPackage};

import ${entityPackage}.${className};
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * ${table.remarks!table.tableName} Mapper 接口
 * 
 * @author ${author}
 * @date ${date}
 */
@Mapper
public interface ${mapperName} <#if config.mapper?? && config.mapper.superClass!?has_content>extends ${config.mapper.superClass}<${className}></#if> {

    /**
     * 根据主键查询
     */
    ${className} selectByPrimaryKey(<#if primaryKey??>@Param("${underscoreToCamelCase(primaryKey.columnName, false)}") ${getJavaType(primaryKey.typeName)} ${underscoreToCamelCase(primaryKey.columnName, false)}</#if>);

    /**
     * 查询所有记录
     */
    List<${className}> selectAll();

    /**
     * 插入记录
     */
    int insert(${className} ${variableName});

    /**
     * 批量插入
     */
    int insertBatch(List<${className}> list);

    /**
     * 根据主键更新
     */
    int updateByPrimaryKey(${className} ${variableName});

    /**
     * 根据主键删除
     */
    int deleteByPrimaryKey(<#if primaryKey??>@Param("${underscoreToCamelCase(primaryKey.columnName, false)}") ${getJavaType(primaryKey.typeName)} ${underscoreToCamelCase(primaryKey.columnName, false)}</#if>);

    /**
     * 根据条件查询
     */
    List<${className}> selectByCondition(${className} ${variableName});
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
        <#return "java.math.BigDecimal">
    <#elseif dbType?contains("DOUBLE") || dbType?contains("FLOAT")>
        <#return "Double">
    <#elseif dbType?contains("DATETIME") || dbType?contains("TIMESTAMP")>
        <#return "java.time.LocalDateTime">
    <#elseif dbType?contains("DATE")>
        <#return "java.time.LocalDate">
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