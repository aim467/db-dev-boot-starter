package com.dbdev.example.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DataSourceServiceExample {

    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate h2JdbcTemplate;

    public DataSourceServiceExample(
            @org.springframework.beans.factory.annotation.Qualifier("mysqlDataSource") javax.sql.DataSource mysqlDataSource,
            @org.springframework.beans.factory.annotation.Qualifier("h2DataSource") javax.sql.DataSource h2DataSource) {
        this.mysqlJdbcTemplate = new JdbcTemplate(mysqlDataSource);
        this.h2JdbcTemplate = new JdbcTemplate(h2DataSource);
    }

    @com.dbdev.example.aspect.DataSource("mysql")
    public List<Map<String, Object>> getMysqlData() {
        return mysqlJdbcTemplate.queryForList("SELECT 1 as test");
    }

    @com.dbdev.example.aspect.DataSource("h2")
    public List<Map<String, Object>> getH2Data() {
        return h2JdbcTemplate.queryForList("SELECT 1 as test");
    }

    public List<Map<String, Object>> getMysqlTables() {
        return mysqlJdbcTemplate.queryForList("SHOW TABLES");
    }

    public List<Map<String, Object>> getH2Tables() {
        return h2JdbcTemplate.queryForList("SHOW TABLES");
    }
}