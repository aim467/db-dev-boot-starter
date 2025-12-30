package com.dbdev.codegen.service;

import com.dbdev.codegen.model.CodeGenConfig;
import com.dbdev.core.model.TableMetadata;
import com.dbdev.core.service.DataSourceService;
import com.dbdev.core.service.MetadataService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 代码生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGenService {

    private final DataSourceService dataSourceService;
    private final MetadataService metadataService;
    private final TemplateEngineService templateEngineService;

    /**
     * 生成代码
     */
    public Map<String, String> generateCode(CodeGenConfig config) throws Exception {
        // 获取数据源
        DataSource dataSource = dataSourceService.getDataSource(config.getDataSourceName());
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在: " + config.getDataSourceName());
        }
        
        // 获取表元数据
        TableMetadata table = metadataService.getTableDetail(dataSource, config.getTableName());
        if (table == null) {
            throw new IllegalArgumentException("表不存在: " + config.getTableName());
        }

        Map<String, String> generatedFiles = new LinkedHashMap<>();
        List<String> generateTypes = config.getGenerateTypes();
        
        // 如果为空或包含ALL，则生成全部
        boolean generateAll = generateTypes == null || generateTypes.isEmpty() 
                || generateTypes.stream().anyMatch(t -> "ALL".equalsIgnoreCase(t));

        // 根据类型生成代码
        if (generateAll || generateTypes.stream().anyMatch(t -> "ENTITY".equalsIgnoreCase(t))) {
            generatedFiles.put("Entity", generateEntity(table, config));
        }
        if (generateAll || generateTypes.stream().anyMatch(t -> "MAPPER".equalsIgnoreCase(t))) {
            generatedFiles.put("Mapper", generateMapper(table, config));
        }
        if (generateAll || generateTypes.stream().anyMatch(t -> "XML".equalsIgnoreCase(t))) {
            generatedFiles.put("XML", generateXml(table, config));
        }
        if (generateAll || generateTypes.stream().anyMatch(t -> "REPOSITORY".equalsIgnoreCase(t))) {
            generatedFiles.put("Repository", generateRepository(table, config));
        }

        return generatedFiles;
    }

    /**
     * 生成 Entity 并保存到文件
     */
    private String generateEntity(TableMetadata table, CodeGenConfig config) throws IOException, TemplateException {
        String code = templateEngineService.generateEntity(table, config);
        if (config.getOutputDir() != null) {
            saveToFile(config, "entity", code);
        }
        return code;
    }

    /**
     * 生成 Mapper 并保存到文件
     */
    private String generateMapper(TableMetadata table, CodeGenConfig config) throws IOException, TemplateException {
        String code = templateEngineService.generateMapper(table, config);
        if (config.getOutputDir() != null) {
            saveToFile(config, "mapper", code);
        }
        return code;
    }

    /**
     * 生成 XML 并保存到文件
     */
    private String generateXml(TableMetadata table, CodeGenConfig config) throws IOException, TemplateException {
        String code = templateEngineService.generateXml(table, config);
        if (config.getOutputDir() != null) {
            saveToFile(config, "xml", code);
        }
        return code;
    }

    /**
     * 生成 Repository 并保存到文件
     */
    private String generateRepository(TableMetadata table, CodeGenConfig config) throws IOException, TemplateException {
        String code = templateEngineService.generateRepository(table, config);
        if (config.getOutputDir() != null) {
            saveToFile(config, "repository", code);
        }
        return code;
    }

    /**
     * 保存代码到文件
     */
    private void saveToFile(CodeGenConfig config, String type, String content) throws IOException {
        // 构建文件路径
        String className = underscoreToCamelCase(config.getTableName(), true);
        String baseDir = config.getOutputDir();
        String packagePath = config.getBasePackage().replace(".", File.separator);
        
        String filePath;
        String fileName;
        
        switch (type) {
            case "entity":
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "entity").toString();
                fileName = className + ".java";
                break;
            case "mapper":
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "mapper").toString();
                fileName = className + (config.getMapper() != null ? config.getMapper().getSuffix() : "Mapper") + ".java";
                break;
            case "xml":
                String xmlDir = config.getXml() != null ? config.getXml().getDirectory() : "mapper";
                filePath = Paths.get(baseDir, "src", "main", "resources", xmlDir).toString();
                fileName = className + (config.getMapper() != null ? config.getMapper().getSuffix() : "Mapper") + ".xml";
                break;
            case "repository":
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "repository").toString();
                fileName = className + (config.getRepository() != null ? config.getRepository().getSuffix() : "Repository") + ".java";
                break;
            default:
                throw new IllegalArgumentException("未知的生成类型: " + type);
        }
        
        // 创建目录
        Path path = Paths.get(filePath);
        Files.createDirectories(path);
        
        // 写入文件
        File file = new File(path.toFile(), fileName);
        if (file.exists() && !Boolean.TRUE.equals(config.getOverwrite())) {
            log.warn("文件已存在，跳过: {}", file.getAbsolutePath());
            return;
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            log.info("文件生成成功: {}", file.getAbsolutePath());
        }
    }

    /**
     * 下划线命名转驼峰命名
     */
    private String underscoreToCamelCase(String underscore, boolean firstCharUpper) {
        if (underscore == null || underscore.isEmpty()) {
            return underscore;
        }
        
        String[] parts = underscore.split("_");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            
            if (i == 0 && !firstCharUpper) {
                result.append(part.substring(0, 1).toLowerCase())
                      .append(part.substring(1).toLowerCase());
            } else {
                result.append(part.substring(0, 1).toUpperCase())
                      .append(part.substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
}