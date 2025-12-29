package ${repositoryPackage};

import ${entityPackage}.${className};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
<#if primaryKey??>
<#assign pkType = getJavaType(primaryKey.typeName)>
<#if pkType == "BigDecimal">
import java.math.BigDecimal;
<#elseif pkType == "LocalDateTime">
import java.time.LocalDateTime;
<#elseif pkType == "LocalDate">
import java.time.LocalDate;
</#if>
</#if>

/**
 * ${table.remarks!table.tableName} Repository 接口
 * 
 * @author ${author}
 * @date ${date}
 */
@Repository
public interface ${repositoryName} extends JpaRepository<${className}, <#if primaryKey??>${pkType}<#else>Long</#if>>, JpaSpecificationExecutor<${className}> {

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