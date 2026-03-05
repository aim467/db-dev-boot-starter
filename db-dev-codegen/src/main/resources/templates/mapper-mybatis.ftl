package ${mapperPackage};

import ${entityPackage}.${className};
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ${table.remarks!table.tableName} Mapper
 */
@Mapper
public interface ${mapperName}<#if config.mapper.superClass?? && config.mapper.superClass?has_content> extends ${config.mapper.superClass}<${className}></#if> {

    ${className} selectById(@Param("id") ${pkType} id);

    List<${className}> selectAll();

    int insert(${className} ${variableName});

    int updateById(${className} ${variableName});

    int deleteById(@Param("id") ${pkType} id);
}
