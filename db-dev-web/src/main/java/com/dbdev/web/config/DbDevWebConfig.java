package com.dbdev.web.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

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

        // 静态资源映射，支持 SPA fallback
        registry.addResourceHandler(uiPath + "/**")
                .addResourceLocations("classpath:/static/db-dev/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);

                        // 如果资源存在，直接返回（CSS、JS、图片等）
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // 如果资源不存在，返回 index.html（SPA fallback）
                        return new ClassPathResource("static/db-dev/index.html");
                    }
                });

        log.info("DB Dev UI resource handler registered: {} -> classpath:/static/db-dev/", uiPath);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // 主页重定向 - 访问 /db-dev 时重定向到 /db-dev/
        registry.addRedirectViewController(uiPath, uiPath + "/index.html");

        log.info("DB Dev UI view controller registered: {}", uiPath);
    }
}
