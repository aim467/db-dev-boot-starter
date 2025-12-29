package com.dbdev.web.config;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${db.dev.ui-path}")
    private String urlPath;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 为所有 DB Dev 的 Controller 添加统一前缀
        configurer.addPathPrefix(urlPath,
                c -> c.getPackageName().startsWith("com.dbdev.web.controller") ||
                        c.getPackageName().startsWith("com.dbdev.codegen.controller"));
    }
}
