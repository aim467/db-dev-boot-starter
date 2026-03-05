package ${entityPackage};

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
<#if config.entity.useLombok>
import lombok.Data;
</#if>

/**
 * ${table.remarks!table.tableName}
 *
 * @author ${author}
 * @date ${date}
 */
@Entity
@Table(name = "${table.tableName}")
<#if config.entity.useLombok>
@Data
</#if>
public class ${className}<#if config.entity.superClass?? && config.entity.superClass?has_content> extends ${config.entity.superClass}</#if> {

<#list table.columns as column>
    /**
     * ${column.remarks!column.columnName}
     */
    <#if primaryKeys?? && primaryKeys?seq_contains(column.columnName)>
    @Id
    <#if column.autoIncrement>
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    </#if>
    @Column(name = "${column.columnName}")
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
