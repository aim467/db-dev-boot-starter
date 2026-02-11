package com.dbdev.web.controller;

import com.dbdev.core.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootVersion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class IndexController extends BaseController {

    @GetMapping("/info")
    public Map<String, Object> info() {
        return buildSystemInfo();
    }

    @GetMapping("/api/system/info")
    public Result<Map<String, Object>> systemInfo() {
        Map<String, Object> info = buildSystemInfo();
        return Result.success(info);
    }

    private Map<String, Object> buildSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "DB Dev - Database Development Assistant");
        info.put("version", "1.0.0-SNAPSHOT");
        info.put("status", "running");
        info.put("springBootVersion", SpringBootVersion.getVersion());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("jvmName", System.getProperty("java.vm.name"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osArch", System.getProperty("os.arch"));
        info.put("userTimezone", System.getProperty("user.timezone"));
        info.put("currentTime", LocalDateTime.now().toString());
        return info;
    }
}
