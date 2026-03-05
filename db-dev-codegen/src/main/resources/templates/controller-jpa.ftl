package ${controllerPackage};

import ${entityPackage}.${className};
import ${repositoryPackage}.${repositoryName};
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ${table.remarks!table.tableName} Controller
 */
@RestController
@RequestMapping("${controllerBasePath}")
public class ${controllerName} {

    private final ${repositoryName} ${variableName}Repository;

    public ${controllerName}(${repositoryName} ${variableName}Repository) {
        this.${variableName}Repository = ${variableName}Repository;
    }

    @GetMapping("/{id}")
    public ${className} getById(@PathVariable ${pkType} id) {
        return ${variableName}Repository.findById(id).orElse(null);
    }

    @GetMapping
    public List<${className}> list() {
        return ${variableName}Repository.findAll();
    }

    @PostMapping
    public ${className} save(@RequestBody ${className} ${variableName}) {
        return ${variableName}Repository.save(${variableName});
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable ${pkType} id) {
        ${variableName}Repository.deleteById(id);
    }
}
