package ${serviceImplPackage};

import ${entityPackage}.${className};
import ${mapperPackage}.${mapperName};
<#if config.service.generateInterface>
import ${servicePackage}.${serviceName};
</#if>
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${table.remarks!table.tableName} Service 实现
 */
@Service
public class <#if config.service.generateInterface>${serviceImplName}<#else>${serviceName}</#if>
        extends ServiceImpl<${mapperName}, ${className}>
        <#if config.service.generateInterface>implements ${serviceName}</#if> {

    <#if config.service.generateInterface>@Override</#if>
    public ${className} getById(${pkType} id) {
        return baseMapper.selectById(id);
    }

    <#if config.service.generateInterface>@Override</#if>
    public List<${className}> listAll() {
        return this.list();
    }

    <#if config.service.generateInterface>@Override</#if>
    public boolean save(${className} ${variableName}) {
        return this.saveOrUpdate(${variableName});
    }

    <#if config.service.generateInterface>@Override</#if>
    public boolean update(${className} ${variableName}) {
        return this.updateById(${variableName});
    }

    <#if config.service.generateInterface>@Override</#if>
    public boolean removeById(${pkType} id) {
        return this.removeById(id);
    }
}
