package com.dbdev.codegen.model;

import lombok.Data;

/**
 * 代码生成配置
 */
@Data
public class CodeGenConfig {
    /**
     * 数据源名称
     */
    private String dataSourceName;
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 基础包名
     */
    private String basePackage;
    
    /**
     * 作者名
     */
    private String author;
    
    /**
     * 输出目录
     */
    private String outputDir;
    
    /**
     * 生成类型：ENTITY, MAPPER, XML, REPOSITORY, ALL
     */
    private String generateType;
    
    /**
     * 是否覆盖已存在文件
     */
    private Boolean overwrite;
    
    /**
     * Entity 配置
     */
    private EntityConfig entity;
    
    /**
     * Mapper 配置
     */
    private MapperConfig mapper;
    
    /**
     * XML 配置
     */
    private XmlConfig xml;
    
    /**
     * Repository 配置
     */
    private RepositoryConfig repository;
    
    @Data
    public static class EntityConfig {
        /**
         * 是否使用 Lombok
         */
        private Boolean useLombok = true;
        
        /**
         * 是否使用 Swagger
         */
        private Boolean useSwagger = false;
        
        /**
         * 是否使用 JPA 注解
         */
        private Boolean useJpa = false;
        
        /**
         * 父类全限定名
         */
        private String superClass;
    }
    
    @Data
    public static class MapperConfig {
        /**
         * Mapper 接口后缀
         */
        private String suffix = "Mapper";
        
        /**
         * 父类全限定名
         */
        private String superClass;
    }
    
    @Data
    public static class XmlConfig {
        /**
         * XML 文件存放目录
         */
        private String directory = "mapper";
    }
    
    @Data
    public static class RepositoryConfig {
        /**
         * Repository 后缀
         */
        private String suffix = "Repository";
        
        /**
         * 是否使用 JPA
         */
        private Boolean useJpa = true;
    }
}