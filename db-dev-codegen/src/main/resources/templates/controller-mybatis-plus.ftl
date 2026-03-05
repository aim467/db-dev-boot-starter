package ${controllerPackage};

import ${entityPackage}.${className};
import ${servicePackage}.${serviceName};
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ${table.remarks!table.tableName} Controller
 */
@RestController
@RequestMapping("${controllerBasePath}")
public class ${controllerName} {

    private final ${serviceName} ${variableName}Service;

    public ${controllerName}(${serviceName} ${variableName}Service) {
        this.${variableName}Service = ${variableName}Service;
    }

    @GetMapping("/{id}")
    public ${className} getById(@PathVariable ${pkType} id) {
        return ${variableName}Service.getById(id);
    }

    @GetMapping
    public List<${className}> list() {
        return ${variableName}Service.listAll();
    }

    @PostMapping
    public boolean save(@RequestBody ${className} ${variableName}) {
        return ${variableName}Service.save(${variableName});
    }

    @PutMapping
    public boolean update(@RequestBody ${className} ${variableName}) {
        return ${variableName}Service.update(${variableName});
    }

    @DeleteMapping("/{id}")
    public boolean remove(@PathVariable ${pkType} id) {
        return ${variableName}Service.removeById(id);
    }
}
