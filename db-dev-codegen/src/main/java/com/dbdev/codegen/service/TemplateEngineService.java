package com.dbdev.codegen.service;

import com.dbdev.codegen.model.CodeGenConfig;
import com.dbdev.core.model.ColumnMetadata;
import com.dbdev.core.model.TableMetadata;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * FreeMarker 模板引擎服务
 */
@Slf4j
@Service
public class TemplateEngineService {

    private final Configuration configuration;

    public TemplateEngineService() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_32);
        this.configuration.setClassForTemplateLoading(this.getClass(), "/templates");
        this.configuration.setDefaultEncoding("UTF-8");
        this.configuration.setNumberFormat("computer");
    }

    /**
     * 生成 Entity 代码
     */
    public String generateEntity(TableMetadata table, CodeGenConfig config) throws IOException, TemplateException {
        Map<String, Object> data = buildTemplateData(table, config);
        return processTemplate("entity.ftl", data);
    }

    /**
     * 生成 Mapper 接口代码
     */
    public String generateMapper(TableMetadata table, CodeGenConfig config) throws IOException, TemplateException {
        Map<String, Object> data = buildTemplateData(table, config);
        return processTemplate("mapper.ftl", data);
    }

    /**
     * 生成 MyBatis XML 代码
     */
    public String generateXml(TableMetadata table, CodeGenConfig config) throws IOException, TemplateException {
        Map<String, Object> data = buildTemplateData(table, config);
        return processTemplate("mapper-xml.ftl", data);
    }

    /**
     * 生成 Repository 代码
     */
    public String generateRepository(TableMetadata table, CodeGenConfig config) throws IOException, TemplateException {
        Map<String, Object> data = buildTemplateData(table, config);
        return processTemplate("repository.ftl", data);
    }

    /**
     * 处理模板
     */
    private String processTemplate(String templateName, Map<String, Object> data) throws IOException, TemplateException {
        Template template = configuration.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(data, writer);
        return writer.toString();
    }

    /**
     * 构建模板数据
     */
    private Map<String, Object> buildTemplateData(TableMetadata table, CodeGenConfig config) {
        Map<String, Object> data = new HashMap<>();
        
        // 基础信息
        data.put("config", config);
        data.put("table", table);
        data.put("author", config.getAuthor());
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 包信息
        data.put("basePackage", config.getBasePackage());
        data.put("entityPackage", config.getBasePackage() + ".entity");
        data.put("mapperPackage", config.getBasePackage() + ".mapper");
        data.put("repositoryPackage", config.getBasePackage() + ".repository");
        
        // 类名
        String className = underscoreToCamelCase(table.getTableName(), true);
        data.put("className", className);
        data.put("entityName", className);
        data.put("mapperName", className + (config.getMapper() != null ? config.getMapper().getSuffix() : "Mapper"));
        data.put("repositoryName", className + (config.getRepository() != null ? config.getRepository().getSuffix() : "Repository"));
        data.put("variableName", underscoreToCamelCase(table.getTableName(), false));
        
        // 主键信息
        ColumnMetadata primaryKey = null;
        if (table.getPrimaryKeys() != null && !table.getPrimaryKeys().isEmpty()) {
            String firstPrimaryKey = table.getPrimaryKeys().get(0);
            primaryKey = table.getColumns().stream()
                    .filter(col -> col.getColumnName().equals(firstPrimaryKey))
                    .findFirst()
                    .orElse(null);
        }
        data.put("primaryKey", primaryKey);
        data.put("primaryKeys", table.getPrimaryKeys());
        
        return data;
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