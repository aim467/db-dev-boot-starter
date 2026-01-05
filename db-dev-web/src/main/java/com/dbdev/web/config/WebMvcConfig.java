package com.dbdev.web.config;

import com.dbdev.web.interceptor.SecurityInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
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
    
    private final SecurityInterceptor securityInterceptor;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 为所有 DB Dev 的 Controller 添加统一前缀
        configurer.addPathPrefix(urlPath,
                c -> c.getPackageName().startsWith("com.dbdev.web.controller") ||
                        c.getPackageName().startsWith("com.dbdev.codegen.controller"));
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册安全拦截器，拦截所有 /api/ 路径（除了登录接口和安全状态接口）
        registry.addInterceptor(securityInterceptor)
                .addPathPatterns(urlPath + "/api/**")
                .excludePathPatterns(
                        urlPath + "/api/auth/login",
                        urlPath + "/api/auth/validate",
                        urlPath + "/api/auth/security-status"
                );
    }
}
