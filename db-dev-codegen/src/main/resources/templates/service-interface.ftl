package ${servicePackage};

import ${entityPackage}.${className};

import java.util.List;

/**
 * ${table.remarks!table.tableName} Service
 */
public interface ${serviceName} {

    ${className} getById(${pkType} id);

    List<${className}> listAll();

    boolean save(${className} ${variableName});

    boolean update(${className} ${variableName});

    boolean removeById(${pkType} id);
}
