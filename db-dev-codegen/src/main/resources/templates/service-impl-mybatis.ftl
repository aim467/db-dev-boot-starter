package ${serviceImplPackage};

import ${entityPackage}.${className};
import ${mapperPackage}.${mapperName};
<#if config.service.generateInterface>
import ${servicePackage}.${serviceName};
</#if>
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * ${table.remarks!table.tableName} Service 实现
 */
@Service
public class <#if config.service.generateInterface>${serviceImplName}<#else>${serviceName}</#if><#if config.service.generateInterface> implements ${serviceName}</#if> {

    private final ${mapperName} ${variableName}Mapper;

    public <#if config.service.generateInterface>${serviceImplName}<#else>${serviceName}</#if>(${mapperName} ${variableName}Mapper) {
        this.${variableName}Mapper = ${variableName}Mapper;
    }

    <#if config.service.generateInterface>@Override</#if>
    public ${className} getById(${pkType} id) {
        return ${variableName}Mapper.selectById(id);
    }

    <#if config.service.generateInterface>@Override</#if>
    public List<${className}> listAll() {
        return ${variableName}Mapper.selectAll();
    }

    <#if config.service.generateInterface>@Override</#if>
    public boolean save(${className} ${variableName}) {
        return ${variableName}Mapper.insert(${variableName}) > 0;
    }

    <#if config.service.generateInterface>@Override</#if>
    public boolean update(${className} ${variableName}) {
        return ${variableName}Mapper.updateById(${variableName}) > 0;
    }

    <#if config.service.generateInterface>@Override</#if>
    public boolean removeById(${pkType} id) {
        return ${variableName}Mapper.deleteById(id) > 0;
    }
}
