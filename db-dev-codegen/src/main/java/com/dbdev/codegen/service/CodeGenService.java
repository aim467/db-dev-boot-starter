package com.dbdev.codegen.service;

import com.dbdev.codegen.model.CodeGenConfig;
import com.dbdev.core.model.TableMetadata;
import com.dbdev.core.service.DataSourceService;
import com.dbdev.core.service.MetadataService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGenService {

    private static final String FRAMEWORK_MYBATIS = "MYBATIS";
    private static final String FRAMEWORK_MYBATIS_PLUS = "MYBATIS_PLUS";
    private static final String FRAMEWORK_JPA = "JPA";

    private static final List<String> ORDER_MYBATIS = List.of("ENTITY", "MAPPER", "XML", "SERVICE", "CONTROLLER");
    private static final List<String> ORDER_JPA = List.of("ENTITY", "REPOSITORY", "CONTROLLER");

    private final DataSourceService dataSourceService;
    private final MetadataService metadataService;
    private final TemplateEngineService templateEngineService;

    @Value("${codegen.temp-dir:${user.home}/codegen}")
    private String tempDir;

    @Value("${codegen.temp-file-ttl:1}")
    private int tempFileTtlHours;

    public Map<String, String> generateCode(CodeGenConfig config) throws Exception {
        initDefaultConfig(config);

        DataSource dataSource = dataSourceService.getDataSource(config.getDataSourceName());
        if (dataSource == null) {
            throw new IllegalArgumentException("数据源不存在: " + config.getDataSourceName());
        }

        TableMetadata table = metadataService.getTableDetail(dataSource, config.getTableName());
        if (table == null) {
            throw new IllegalArgumentException("表不存在: " + config.getTableName());
        }

        String frameworkType = normalizeFramework(config.getGenerationType());
        List<String> components = resolveComponents(config, frameworkType);

        Map<String, String> generatedFiles = new LinkedHashMap<>();
        boolean generateServiceInterface = Boolean.TRUE.equals(config.getService().getGenerateInterface());

        for (String component : components) {
            switch (component) {
                case "ENTITY" -> generatedFiles.put("entity", generateEntity(table, config, frameworkType));
                case "MAPPER" -> generatedFiles.put("mapper", generateMapper(table, config, frameworkType));
                case "XML" -> generatedFiles.put("xml", generateXml(table, config, frameworkType));
                case "REPOSITORY" -> generatedFiles.put("repository", generateRepository(table, config));
                case "SERVICE" -> {
                    if (FRAMEWORK_JPA.equals(frameworkType)) {
                        continue;
                    }
                    if (generateServiceInterface) {
                        generatedFiles.put("service", generateServiceInterface(table, config, frameworkType));
                        generatedFiles.put("serviceImpl", generateServiceImpl(table, config, frameworkType));
                    } else {
                        generatedFiles.put("service", generateServiceImpl(table, config, frameworkType));
                    }
                }
                case "CONTROLLER" -> generatedFiles.put("controller", generateController(table, config, frameworkType));
                default -> throw new IllegalArgumentException("不支持的组件类型: " + component);
            }
        }

        return generatedFiles;
    }

    private String generateEntity(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        String code = templateEngineService.generateEntity(table, config, frameworkType);
        if (config.getOutputDir() != null) {
            saveToFile(config, "entity", code);
        }
        return code;
    }

    private String generateMapper(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        String code = templateEngineService.generateMapper(table, config, frameworkType);
        if (config.getOutputDir() != null) {
            saveToFile(config, "mapper", code);
        }
        return code;
    }

    private String generateXml(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        String code = templateEngineService.generateXml(table, config, frameworkType);
        if (config.getOutputDir() != null) {
            saveToFile(config, "xml", code);
        }
        return code;
    }

    private String generateRepository(TableMetadata table, CodeGenConfig config)
            throws IOException, TemplateException {
        String code = templateEngineService.generateRepository(table, config);
        if (config.getOutputDir() != null) {
            saveToFile(config, "repository", code);
        }
        return code;
    }

    private String generateServiceInterface(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        String code = templateEngineService.generateServiceInterface(table, config, frameworkType);
        if (config.getOutputDir() != null) {
            saveToFile(config, "serviceInterface", code);
        }
        return code;
    }

    private String generateServiceImpl(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        String code = templateEngineService.generateServiceImpl(table, config, frameworkType);
        if (config.getOutputDir() != null) {
            if (Boolean.TRUE.equals(config.getService().getGenerateInterface())) {
                saveToFile(config, "serviceImpl", code);
            } else {
                saveToFile(config, "service", code);
            }
        }
        return code;
    }

    private String generateController(TableMetadata table, CodeGenConfig config, String frameworkType)
            throws IOException, TemplateException {
        String code = templateEngineService.generateController(table, config, frameworkType);
        if (config.getOutputDir() != null) {
            saveToFile(config, "controller", code);
        }
        return code;
    }

    private void saveToFile(CodeGenConfig config, String type, String content) throws IOException {
        String className = underscoreToCamelCase(config.getTableName(), true);
        String baseDir = config.getOutputDir();
        String packagePath = config.getBasePackage().replace(".", File.separator);

        String filePath;
        String fileName;

        String mapperSuffix = config.getMapper().getSuffix();
        String repoSuffix = config.getRepository().getSuffix();
        String serviceSuffix = config.getService().getInterfaceSuffix();
        String serviceImplSuffix = config.getService().getImplSuffix();
        String controllerSuffix = config.getController().getSuffix();

        switch (type) {
            case "entity" -> {
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "entity").toString();
                fileName = className + ".java";
            }
            case "mapper" -> {
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "mapper").toString();
                fileName = className + mapperSuffix + ".java";
            }
            case "xml" -> {
                String xmlDir = config.getXml().getDirectory();
                filePath = Paths.get(baseDir, "src", "main", "resources", xmlDir).toString();
                fileName = className + mapperSuffix + ".xml";
            }
            case "repository" -> {
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "repository").toString();
                fileName = className + repoSuffix + ".java";
            }
            case "serviceInterface" -> {
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "service").toString();
                fileName = className + serviceSuffix + ".java";
            }
            case "serviceImpl" -> {
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "service", "impl").toString();
                fileName = className + serviceImplSuffix + ".java";
            }
            case "service" -> {
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "service").toString();
                fileName = className + serviceSuffix + ".java";
            }
            case "controller" -> {
                filePath = Paths.get(baseDir, "src", "main", "java", packagePath, "controller").toString();
                fileName = className + controllerSuffix + ".java";
            }
            default -> throw new IllegalArgumentException("未知的生成类型: " + type);
        }

        Path path = Paths.get(filePath);
        Files.createDirectories(path);

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

    public String generateAndPackage(CodeGenConfig config) throws Exception {
        Map<String, String> generatedFiles = generateCode(config);
        return createZipPackage(generatedFiles, config);
    }

    private String createZipPackage(Map<String, String> files, CodeGenConfig config) throws IOException {
        Path tempPath = Paths.get(tempDir);
        Files.createDirectories(tempPath);

        String className = underscoreToCamelCase(config.getTableName(), true);
        String zipFileName = className + "_" + System.currentTimeMillis() + ".zip";
        Path zipFilePath = tempPath.resolve(zipFileName);

        String packagePath = config.getBasePackage().replace(".", "/");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String entryPath = getZipEntryPath(entry.getKey(), className, packagePath, config);
                ZipEntry zipEntry = new ZipEntry(entryPath);
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }

        log.info("ZIP 文件生成成功: {}", zipFilePath);
        return zipFileName;
    }

    private String getZipEntryPath(String type, String className, String packagePath, CodeGenConfig config) {
        String mapperSuffix = config.getMapper().getSuffix();
        String repoSuffix = config.getRepository().getSuffix();
        String serviceSuffix = config.getService().getInterfaceSuffix();
        String serviceImplSuffix = config.getService().getImplSuffix();
        String controllerSuffix = config.getController().getSuffix();
        String xmlDir = config.getXml().getDirectory();

        return switch (type.toLowerCase(Locale.ROOT)) {
            case "entity" -> "src/main/java/" + packagePath + "/entity/" + className + ".java";
            case "mapper" -> "src/main/java/" + packagePath + "/mapper/" + className + mapperSuffix + ".java";
            case "xml" -> "src/main/resources/" + xmlDir + "/" + className + mapperSuffix + ".xml";
            case "repository" -> "src/main/java/" + packagePath + "/repository/" + className + repoSuffix + ".java";
            case "service" -> "src/main/java/" + packagePath + "/service/" + className + serviceSuffix + ".java";
            case "serviceimpl" -> "src/main/java/" + packagePath + "/service/impl/" + className + serviceImplSuffix + ".java";
            case "controller" -> "src/main/java/" + packagePath + "/controller/" + className + controllerSuffix + ".java";
            default -> "src/main/resources/" + type + ".txt";
        };
    }

    public Path getZipFilePath(String fileName) {
        return Paths.get(tempDir).resolve(fileName);
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredFiles() {
        try {
            Path tempPath = Paths.get(tempDir);
            if (!Files.exists(tempPath)) {
                return;
            }

            Instant cutoff = Instant.now().minus(tempFileTtlHours, ChronoUnit.HOURS);

            Files.list(tempPath)
                    .filter(path -> path.toString().endsWith(".zip"))
                    .forEach(path -> {
                        try {
                            Instant fileTime = Files.getLastModifiedTime(path).toInstant();
                            if (fileTime.isBefore(cutoff)) {
                                Files.delete(path);
                                log.info("已清理过期文件: {}", path);
                            }
                        } catch (IOException e) {
                            log.warn("清理文件失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("清理临时文件目录失败", e);
        }
    }

    private List<String> resolveComponents(CodeGenConfig config, String frameworkType) {
        Set<String> requested = new LinkedHashSet<>();

        if (config.getComponents() != null && !config.getComponents().isEmpty()) {
            requested.addAll(config.getComponents().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(s -> s.trim().toUpperCase(Locale.ROOT))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else if (config.getGenerateTypes() != null && !config.getGenerateTypes().isEmpty()) {
            boolean all = config.getGenerateTypes().stream().anyMatch(t -> "ALL".equalsIgnoreCase(t));
            if (all) {
                requested.addAll(defaultComponents(frameworkType));
            } else {
                requested.addAll(config.getGenerateTypes().stream()
                        .filter(s -> s != null && !s.isBlank())
                        .map(s -> s.trim().toUpperCase(Locale.ROOT))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
            }
        } else {
            requested.addAll(defaultComponents(frameworkType));
        }

        List<String> ordered = FRAMEWORK_JPA.equals(frameworkType) ? ORDER_JPA : ORDER_MYBATIS;
        List<String> result = new ArrayList<>();
        for (String component : ordered) {
            if (requested.contains(component)) {
                result.add(component);
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("未选择可生成组件");
        }

        return result;
    }

    private List<String> defaultComponents(String frameworkType) {
        if (FRAMEWORK_JPA.equals(frameworkType)) {
            return ORDER_JPA;
        }
        return ORDER_MYBATIS;
    }

    private void initDefaultConfig(CodeGenConfig config) {
        if (config.getEntity() == null) {
            config.setEntity(new CodeGenConfig.EntityConfig());
        }
        if (config.getMapper() == null) {
            config.setMapper(new CodeGenConfig.MapperConfig());
        }
        if (config.getXml() == null) {
            config.setXml(new CodeGenConfig.XmlConfig());
        }
        if (config.getRepository() == null) {
            config.setRepository(new CodeGenConfig.RepositoryConfig());
        }
        if (config.getService() == null) {
            config.setService(new CodeGenConfig.ServiceConfig());
        }
        if (config.getController() == null) {
            config.setController(new CodeGenConfig.ControllerConfig());
        }

        if (config.getMapper().getSuffix() == null || config.getMapper().getSuffix().isBlank()) {
            config.getMapper().setSuffix("Mapper");
        }
        if (config.getRepository().getSuffix() == null || config.getRepository().getSuffix().isBlank()) {
            config.getRepository().setSuffix("Repository");
        }
        if (config.getXml().getDirectory() == null || config.getXml().getDirectory().isBlank()) {
            config.getXml().setDirectory("mapper");
        }
        if (config.getService().getInterfaceSuffix() == null || config.getService().getInterfaceSuffix().isBlank()) {
            config.getService().setInterfaceSuffix("Service");
        }
        if (config.getService().getImplSuffix() == null || config.getService().getImplSuffix().isBlank()) {
            config.getService().setImplSuffix("ServiceImpl");
        }
        if (config.getService().getGenerateInterface() == null) {
            config.getService().setGenerateInterface(true);
        }
        if (config.getController().getSuffix() == null || config.getController().getSuffix().isBlank()) {
            config.getController().setSuffix("Controller");
        }
        if (config.getGenerationType() == null || config.getGenerationType().isBlank()) {
            config.setGenerationType(FRAMEWORK_MYBATIS);
        }
    }

    private String normalizeFramework(String frameworkType) {
        String normalized = frameworkType == null ? FRAMEWORK_MYBATIS : frameworkType.trim().toUpperCase(Locale.ROOT);
        if (FRAMEWORK_MYBATIS.equals(normalized) || FRAMEWORK_MYBATIS_PLUS.equals(normalized) || FRAMEWORK_JPA.equals(normalized)) {
            return normalized;
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
