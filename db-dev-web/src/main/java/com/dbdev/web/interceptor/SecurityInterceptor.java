package com.dbdev.web.interceptor;


import com.dbdev.core.response.Result;
import com.dbdev.web.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 安全拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityInterceptor implements HandlerInterceptor {
    
    private final TokenService tokenService;

    @Value("${db.dev.security.enabled:true}")
    private boolean securityEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否启用安全认证
        if (!securityEnabled) {
            return true;
        }
        
        String path = request.getRequestURI();
        
        // 排除登录接口、安全状态接口和静态资源
        if (path.contains("/api/auth/login") || 
            path.contains("/api/auth/validate") ||
            path.contains("/api/auth/security-status") ||
            !path.contains("/api/")) {
            return true;
        }
        
        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return sendUnauthorizedResponse(response, "未提供认证Token");
        }
        
        String token = authHeader.substring(7);
        
        // 验证Token
        if (!tokenService.validateToken(token)) {
            return sendUnauthorizedResponse(response, "Token无效或已过期");
        }
        
        // 刷新Token过期时间
        tokenService.refreshToken(token);
        
        return true;
    }
    
    /**
     * 发送未授权响应
     */
    private boolean sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        Result<Object> result = Result.error(message);
        result.setCode(401);
        
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
        response.getWriter().flush();
        
        return false;
    }
}

