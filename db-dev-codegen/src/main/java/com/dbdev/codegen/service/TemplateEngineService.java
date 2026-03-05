package com.dbdev.codegen.service;

import com.dbdev.codegen.model.CodeGenConfig;
import com.dbdev.core.model.ColumnMetadata;
import com.dbdev.core.model.TableMetadata;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * FreeMarker 模板引擎服务
 */
@Service
public class TemplateEngineService {

    private final Configuration configuration;

    public TemplateEngineService() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_32);
        this.configuration.setClassForTemplateLoading(this.getClass(), "/templates");
        this.configuration.setDefaultEncoding("UTF-8");
        this.configuration.setNumberFormat("computer");
    }

    public String generateEntity(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        return processTemplate(templateByFramework("entity", frameworkType), buildTemplateData(table, config, frameworkType));
    }

    public String generateMapper(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        return processTemplate(templateByFramework("mapper", frameworkType), buildTemplateData(table, config, frameworkType));
    }

    public String generateXml(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        return processTemplate(templateByFramework("xml", frameworkType), buildTemplateData(table, config, frameworkType));
    }

    public String generateRepository(TableMetadata table, CodeGenConfig config)
            throws IOException, TemplateException {
        return processTemplate("repository-jpa.ftl", buildTemplateData(table, config, "JPA"));
    }

    public String generateServiceInterface(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        return processTemplate("service-interface.ftl", buildTemplateData(table, config, frameworkType));
    }

    public String generateServiceImpl(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        return processTemplate(templateByFramework("service-impl", frameworkType), buildTemplateData(table, config, frameworkType));
    }

    public String generateController(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        return processTemplate(templateByFramework("controller", frameworkType), buildTemplateData(table, config, frameworkType));
    }

    private String templateByFramework(String type, String frameworkType) {
        String framework = normalizeFramework(frameworkType);
        if ("JPA".equals(framework)) {
            return switch (type) {
                case "entity" -> "entity-jpa.ftl";
                case "controller" -> "controller-jpa.ftl";
                case "repository" -> "repository-jpa.ftl";
                default -> throw new IllegalArgumentException("JPA 不支持模板类型: " + type);
            };
        }
        if ("MYBATIS_PLUS".equals(framework)) {
            return switch (type) {
                case "entity" -> "entity-mybatis-plus.ftl";
                case "mapper" -> "mapper-mybatis-plus.ftl";
                case "xml" -> "mapper-xml-mybatis-plus.ftl";
                case "service-impl" -> "service-impl-mybatis-plus.ftl";
                case "controller" -> "controller-mybatis-plus.ftl";
                default -> throw new IllegalArgumentException("MyBatis-Plus 不支持模板类型: " + type);
            };
        }
        return switch (type) {
            case "entity" -> "entity-mybatis.ftl";
            case "mapper" -> "mapper-mybatis.ftl";
            case "xml" -> "mapper-xml-mybatis.ftl";
            case "service-impl" -> "service-impl-mybatis.ftl";
            case "controller" -> "controller-mybatis.ftl";
            default -> throw new IllegalArgumentException("MyBatis 不支持模板类型: " + type);
        };
    }

    private String processTemplate(String templateName, Map<String, Object> data) throws IOException, TemplateException {
        Template template = configuration.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(data, writer);
        return writer.toString();
    }

    private Map<String, Object> buildTemplateData(TableMetadata table, CodeGenConfig config, String frameworkType) {
        String framework = normalizeFramework(frameworkType);
        String className = underscoreToCamelCase(table.getTableName(), true);

        Map<String, Object> data = new HashMap<>();
        data.put("config", config);
        data.put("table", table);
        data.put("author", config.getAuthor());
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        data.put("frameworkType", framework);
        data.put("isMybatis", "MYBATIS".equals(framework));
        data.put("isMybatisPlus", "MYBATIS_PLUS".equals(framework));
        data.put("isJpa", "JPA".equals(framework));

        String basePackage = config.getBasePackage();
        data.put("basePackage", basePackage);
        data.put("entityPackage", basePackage + ".entity");
        data.put("mapperPackage", basePackage + ".mapper");
        data.put("repositoryPackage", basePackage + ".repository");
        data.put("servicePackage", basePackage + ".service");
        data.put("serviceImplPackage", basePackage + ".service.impl");
        data.put("controllerPackage", basePackage + ".controller");

        String mapperSuffix = config.getMapper() != null && config.getMapper().getSuffix() != null
                ? config.getMapper().getSuffix() : "Mapper";
        String repositorySuffix = config.getRepository() != null && config.getRepository().getSuffix() != null
                ? config.getRepository().getSuffix() : "Repository";
        String serviceInterfaceSuffix = config.getService() != null && config.getService().getInterfaceSuffix() != null
                ? config.getService().getInterfaceSuffix() : "Service";
        String serviceImplSuffix = config.getService() != null && config.getService().getImplSuffix() != null
                ? config.getService().getImplSuffix() : "ServiceImpl";
        String controllerSuffix = config.getController() != null && config.getController().getSuffix() != null
                ? config.getController().getSuffix() : "Controller";

        data.put("className", className);
        data.put("entityName", className);
        data.put("mapperName", className + mapperSuffix);
        data.put("repositoryName", className + repositorySuffix);
        data.put("serviceName", className + serviceInterfaceSuffix);
        data.put("serviceImplName", className + serviceImplSuffix);
        data.put("controllerName", className + controllerSuffix);
        data.put("variableName", underscoreToCamelCase(table.getTableName(), false));

        String configuredPath = config.getController() != null ? config.getController().getBasePath() : null;
        String defaultPath = "/" + table.getTableName().toLowerCase(Locale.ROOT).replace('_', '-');
        data.put("controllerBasePath", (configuredPath == null || configuredPath.isBlank()) ? defaultPath : configuredPath);

        ColumnMetadata primaryKey = resolvePrimaryKey(table);
        data.put("primaryKey", primaryKey);
        data.put("primaryKeys", table.getPrimaryKeys());
        data.put("pkType", resolveJavaType(primaryKey));

        return data;
    }

    private String resolveJavaType(ColumnMetadata column) {
        if (column == null) {
            return "Long";
        }
        if (column.getJavaType() != null && !column.getJavaType().isBlank()) {
            return simpleJavaType(column.getJavaType());
        }
        String dbType = Objects.toString(column.getTypeName(), "").toUpperCase(Locale.ROOT);
        if (dbType.contains("BIGINT")) {
            return "Long";
        }
        if (dbType.contains("INT") || dbType.contains("TINYINT") || dbType.contains("SMALLINT")) {
            return "Integer";
        }
        if (dbType.contains("DECIMAL") || dbType.contains("NUMERIC")) {
            return "BigDecimal";
        }
        if (dbType.contains("DOUBLE") || dbType.contains("FLOAT")) {
            return "Double";
        }
        if (dbType.contains("DATETIME") || dbType.contains("TIMESTAMP")) {
            return "LocalDateTime";
        }
        if (dbType.contains("DATE")) {
            return "LocalDate";
        }
        if (dbType.contains("BOOLEAN") || dbType.contains("BIT")) {
            return "Boolean";
        }
        return "String";
    }

    private String simpleJavaType(String javaType) {
        if (!javaType.contains(".")) {
            return javaType;
        }
        return javaType.substring(javaType.lastIndexOf('.') + 1);
    }

    private ColumnMetadata resolvePrimaryKey(TableMetadata table) {
        List<String> primaryKeys = table.getPrimaryKeys();
        if (primaryKeys == null || primaryKeys.isEmpty() || table.getColumns() == null) {
            return null;
        }
        String firstPrimaryKey = primaryKeys.get(0);
        return table.getColumns().stream()
                .filter(col -> firstPrimaryKey.equals(col.getColumnName()))
                .findFirst()
                .orElse(null);
    }

    private String normalizeFramework(String frameworkType) {
        if (frameworkType == null || frameworkType.isBlank()) {
            return "MYBATIS";
        }
        String value = frameworkType.trim().toUpperCase(Locale.ROOT);
        if ("MYBATIS_PLUS".equals(value) || "JPA".equals(value) || "MYBATIS".equals(value)) {
            return value;
        }
        throw new IllegalArgumentException("不支持的生成框架类型: " + frameworkType);
    }

    private String underscoreToCamelCase(String underscore, boolean firstCharUpper) {
        if (underscore == null || underscore.isEmpty()) {
            return underscore;
        }

        String[] parts = underscore.split("_");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            if (i == 0 && !firstCharUpper) {
                result.append(part.substring(0, 1).toLowerCase(Locale.ROOT))
                        .append(part.substring(1).toLowerCase(Locale.ROOT));
            } else {
                result.append(part.substring(0, 1).toUpperCase(Locale.ROOT))
                        .append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }

        return result.toString();
    }
}
