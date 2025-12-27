package com.dbdev.web.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * DB Dev Web 配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbDevWebConfig implements WebMvcConfigurer {

    @Value("${db.dev.ui-path}")
    private String uiPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 静态资源映射
        registry.addResourceHandler(uiPath + "/**")
                .addResourceLocations("classpath:/static/db-dev/");

        log.info("DB Dev UI resource handler registered: {} -> classpath:/static/db-dev/", uiPath);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // 主页重定向
        registry.addRedirectViewController(uiPath, uiPath + "/index.html");

        log.info("DB Dev UI view controller registered: {}", uiPath);
    }
}
