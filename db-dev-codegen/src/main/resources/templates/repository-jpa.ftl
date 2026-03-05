package ${repositoryPackage};

import ${entityPackage}.${className};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * ${table.remarks!table.tableName} Repository
 */
@Repository
public interface ${repositoryName} extends JpaRepository<${className}, ${pkType}>, JpaSpecificationExecutor<${className}> {
}
