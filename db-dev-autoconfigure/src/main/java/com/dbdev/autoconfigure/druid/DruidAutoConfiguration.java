package com.dbdev.autoconfigure.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.dbdev.core.druid.DruidDataSourceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Druid 数据源自动配置
 * 当检测到项目使用 Druid 时自动启用
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(DruidDataSource.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "db.dev", name = "enabled", havingValue = "true")
public class DruidAutoConfiguration {

    public DruidAutoConfiguration() {
        log.info("========================================");
        log.info("DB Dev: Druid DataSource Detected!");
        log.info("Druid monitoring features enabled");
        log.info("========================================");
    }

    @Bean
    public DruidDataSourceProvider druidDataSourceProvider(DataSource dataSource) {
        return new DruidDataSourceProvider(dataSource);
    }
}
