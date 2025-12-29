package com.dbdev.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import javax.sql.DataSource;

/**
 * DB Dev 自动配置
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(prefix = "db.dev", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DbDevProperties.class)
@ComponentScan(basePackages = {"com.dbdev.core", "com.dbdev.web", "com.dbdev.codegen"})
public class DbDevAutoConfiguration {

    public DbDevAutoConfiguration(DbDevProperties properties) {
        log.info("========================================");
        log.info("DB Dev Starter Enabled");
        log.info("UI Path: {}", properties.getUiPath());
        log.info("API Base URL: {}/api", properties.getUiPath());
        log.info("========================================");
    }
}
