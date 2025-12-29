<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperPackage}.${mapperName}">

    <resultMap id="BaseResultMap" type="${entityPackage}.${className}">
    <#list table.columns as column>
        <#if primaryKeys?? && primaryKeys?seq_contains(column.columnName)>
        <id column="${column.columnName}" property="${underscoreToCamelCase(column.columnName, false)}" />
        <#else>
        <result column="${column.columnName}" property="${underscoreToCamelCase(column.columnName, false)}" />
        </#if>
    </#list>
    </resultMap>

    <sql id="Base_Column_List">
    <#list table.columns as column>
        ${column.columnName}<#if column_has_next>, </#if>
    </#list>
    </sql>

    <select id="selectByPrimaryKey"
        <#if primaryKey??>
            parameterType="${getJavaType(primaryKey.typeName)}"
            <#assign pkProperty = underscoreToCamelCase(primaryKey.columnName, false)>
        </#if>
        resultMap="BaseResultMap">
        SELECT
            <include refid="Base_Column_List" />
        FROM ${table.tableName}
        WHERE
        <#if primaryKey??>
            ${primaryKey.columnName} = #{${pkProperty}, jdbcType=${primaryKey.jdbcType!'VARCHAR'}}
        </#if>
    </select>

    <select id="selectAll" resultMap="BaseResultMap">
        SELECT 
            <include refid="Base_Column_List" />
        FROM ${table.tableName}
    </select>

    <insert id="insert" parameterType="${entityPackage}.${className}">
        INSERT INTO ${table.tableName} (
    <#list table.columns as column>
            ${column.columnName}<#if column_has_next>,</#if>
    </#list>
        ) VALUES (
    <#list table.columns as column>
            #{underscoreToCamelCase(column.columnName, false)}<#if column_has_next>,</#if>
    </#list>
        )
    </insert>

    <insert id="insertBatch" parameterType="java.util.List">
        INSERT INTO ${table.tableName} (
    <#list table.columns as column>
            ${column.columnName}<#if column_has_next>,</#if>
    </#list>
        ) VALUES 
        <foreach collection="list" item="item" separator=",">
        (
    <#list table.columns as column>
            #{item.underscoreToCamelCase(column.columnName, false)}<#if column_has_next>,</#if>
    </#list>
        )
        </foreach>
    </insert>

    <update id="updateByPrimaryKey" parameterType="${entityPackage}.${className}">
        UPDATE ${table.tableName} 
        SET 
    <#list table.columns as column>
            ${column.columnName} = #{underscoreToCamelCase(column.columnName, false)}<#if column_has_next>,</#if>
    </#list>
        WHERE <#if primaryKey??>${primaryKey.columnName} = #{underscoreToCamelCase(primaryKey.columnName, false)}</#if>
    </update>

    <delete id="deleteByPrimaryKey"<#if primaryKey??> parameterType="${getJavaType(primaryKey.typeName)}"</#if>>
        DELETE FROM ${table.tableName} 
        WHERE <#if primaryKey??>${primaryKey.columnName} = #{underscoreToCamelCase(primaryKey.columnName, false)}</#if>
    </delete>

    <select id="selectByCondition" parameterType="${entityPackage}.${className}" resultMap="BaseResultMap">
        SELECT 
            <include refid="Base_Column_List" />
        FROM ${table.tableName}
        <where>
    <#list table.columns as column>
            <if test="${underscoreToCamelCase(column.columnName, false)} != null">
                AND ${column.columnName} = #{underscoreToCamelCase(column.columnName, false)}
            </if>
    </#list>
        </where>
    </select>

</mapper>

<#-- FreeMarker 函数定义 -->
<#function getJavaType dbType>
    <#if dbType?contains("VARCHAR") || dbType?contains("CHAR") || dbType?contains("TEXT")>
        <#return "java.lang.String">
    <#elseif dbType?contains("INT") || dbType?contains("TINYINT") || dbType?contains("SMALLINT")>
        <#return "java.lang.Integer">
    <#elseif dbType?contains("BIGINT")>
        <#return "java.lang.Long">
    <#elseif dbType?contains("DECIMAL") || dbType?contains("NUMERIC")>
        <#return "java.math.BigDecimal">
    <#elseif dbType?contains("DOUBLE") || dbType?contains("FLOAT")>
        <#return "java.lang.Double">
    <#elseif dbType?contains("DATETIME") || dbType?contains("TIMESTAMP")>
        <#return "java.time.LocalDateTime">
    <#elseif dbType?contains("DATE")>
        <#return "java.time.LocalDate">
    <#elseif dbType?contains("BOOLEAN") || dbType?contains("BIT")>
        <#return "java.lang.Boolean">
    <#else>
        <#return "java.lang.String">
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