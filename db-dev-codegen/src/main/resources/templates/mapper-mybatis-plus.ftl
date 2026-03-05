package ${mapperPackage};

import ${entityPackage}.${className};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ${table.remarks!table.tableName} Mapper
 */
@Mapper
public interface ${mapperName} extends BaseMapper<${className}> {
}
