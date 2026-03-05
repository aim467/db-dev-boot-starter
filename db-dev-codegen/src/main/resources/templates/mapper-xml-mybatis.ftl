<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperPackage}.${mapperName}">

    <resultMap id="BaseResultMap" type="${entityPackage}.${className}">
    <#list table.columns as column>
        <#if primaryKeys?? && primaryKeys?seq_contains(column.columnName)>
        <id column="${column.columnName}" property="${camel(column.columnName)}"/>
        <#else>
        <result column="${column.columnName}" property="${camel(column.columnName)}"/>
        </#if>
    </#list>
    </resultMap>

</mapper>

<#function camel value upper=false>
    <#assign parts = value?split("_")>
    <#assign out = "">
    <#list parts as p>
        <#if p != "">
            <#if p_index == 0 && !upper>
                <#assign out = out + p?lower_case>
            <#else>
                <#assign out = out + p?cap_first>
            </#if>
        </#if>
    </#list>
    <#return out>
</#function>
