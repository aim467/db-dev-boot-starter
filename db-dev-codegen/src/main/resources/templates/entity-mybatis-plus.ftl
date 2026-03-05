package ${entityPackage};

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
<#if config.entity.useLombok>
import lombok.Data;
</#if>

/**
 * ${table.remarks!table.tableName}
 *
 * @author ${author}
 * @date ${date}
 */
@TableName("${table.tableName}")
<#if config.entity.useLombok>
@Data
</#if>
public class ${className}<#if config.entity.superClass?? && config.entity.superClass?has_content> extends ${config.entity.superClass}</#if> {

<#list table.columns as column>
    /**
     * ${column.remarks!column.columnName}
     */
    <#if primaryKeys?? && primaryKeys?seq_contains(column.columnName)>
    @TableId(value = "${column.columnName}", type = IdType.AUTO)
    <#else>
    @TableField("${column.columnName}")
    </#if>
    private ${javaType(column)} ${camel(column.columnName)};

</#list>
}

<#function javaType column>
    <#if column.javaType?? && column.javaType?has_content>
        <#assign t = column.javaType>
        <#if t?contains(".")>
            <#return t?substring(t?last_index_of(".") + 1)>
        </#if>
        <#return t>
    </#if>
    <#return "String">
</#function>

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
