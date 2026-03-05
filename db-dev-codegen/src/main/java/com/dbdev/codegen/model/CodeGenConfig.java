package com.dbdev.codegen.model;

import lombok.Data;

import java.util.List;

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
     * 作者
     */
    private String author;

    /**
     * 输出目录
     */
    private String outputDir;

    /**
     * 生成框架类型：MYBATIS, MYBATIS_PLUS, JPA
     */
    private String generationType = "MYBATIS";

    /**
     * 生成组件列表：ENTITY, MAPPER, XML, SERVICE, CONTROLLER, REPOSITORY
     */
    private List<String> components;

    /**
     * 兼容旧版字段：ENTITY, MAPPER, XML, REPOSITORY
     */
    private List<String> generateTypes;

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

    /**
     * Service 配置
     */
    private ServiceConfig service;

    /**
     * Controller 配置
     */
    private ControllerConfig controller;

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
         * XML 文件目录
         */
        private String directory = "mapper";
    }

    @Data
    public static class RepositoryConfig {
        /**
         * Repository 后缀
         */
        private String suffix = "Repository";
    }

    @Data
    public static class ServiceConfig {
        /**
         * Service 接口后缀
         */
        private String interfaceSuffix = "Service";

        /**
         * Service 实现后缀
         */
        private String implSuffix = "ServiceImpl";

        /**
         * 是否生成 Service 接口
         */
        private Boolean generateInterface = true;
    }

    @Data
    public static class ControllerConfig {
        /**
         * Controller 后缀
         */
        private String suffix = "Controller";

        /**
         * 请求前缀，为空时默认使用表名
         */
        private String basePath;
    }
}
