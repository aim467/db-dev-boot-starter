package com.dbdev.web.controller;


import com.dbdev.core.response.Result;
import com.dbdev.web.service.TokenService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证 Controller
 * 实际路径：{uiPath}/api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final TokenService tokenService;

    @Value("${db.dev.security.enabled:true}")
    private boolean securityEnabled;

    @Value("${db.dev.security.username:admin}")
    private String defaultUsername;

    @Value("${db.dev.security.password:admin}")
    private String defaultPassword;


    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {

        // 检查是否启用安全认证
        if (!securityEnabled) {
            // 如果未启用安全认证，直接返回成功
            LoginResponse response = new LoginResponse();
            response.setToken("disabled");
            response.setUsername("guest");
            return Result.success(response);
        }

        // 验证用户名和密码
        if (request.getUsername() == null || request.getPassword() == null) {
            return Result.error("用户名和密码不能为空");
        }

        if (!request.getUsername().equals(defaultUsername) || !request.getPassword().equals(defaultPassword)) {
            return Result.error("用户名或密码错误");
        }

        // 生成Token
        String token = tokenService.generateToken(request.getUsername());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(request.getUsername());

        return Result.success(response);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenService.removeToken(token);
        }
        return Result.success(null);
    }

    /**
     * 检查安全认证状态
     */
    @GetMapping("/security-status")
    public Result<Map<String, Object>> getSecurityStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", securityEnabled);
        return Result.success(result);
    }

    /**
     * 验证Token
     */
    @GetMapping("/validate")
    public Result<Map<String, Object>> validate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            result.put("valid", false);
            return Result.success(result);
        }

        String token = authHeader.substring(7);
        boolean valid = tokenService.validateToken(token);
        result.put("valid", valid);

        if (valid) {
            String username = tokenService.getUsername(token);
            result.put("username", username);
            // 刷新token过期时间
            tokenService.refreshToken(token);
        }

        return Result.success(result);
    }

    /**
     * 登录请求
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    /**
     * 登录响应
     */
    @Data
    public static class LoginResponse {
        private String token;
        private String username;
    }
}

