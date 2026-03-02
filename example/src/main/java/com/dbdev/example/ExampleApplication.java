package com.dbdev.example;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;

@SpringBootApplication
@RestController
public class ExampleApplication {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @GetMapping("/create-test-session")
    public String createSession(HttpSession session) {
        // 1. 强制往 Session 里放个东西，确保 Session 被创建并标记为活跃
        session.setAttribute("testUser", "druid_tester");

        // 2. 执行一条简单的 SQL，让 Druid 将这条 SQL 关联到当前 Session
        // 如果没有 SQL 执行，Session 可能不会出现在统计列表中，或者显示为 0 计数

        jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        return "Session created! ID: " + session.getId() + ", Please check Druid Web Session page.";
    }

    @GetMapping("/test-wall")
    public String testWall() {
        jdbcTemplate.execute("delete from r_pan_error_log");
        return "OK";
    }
}
