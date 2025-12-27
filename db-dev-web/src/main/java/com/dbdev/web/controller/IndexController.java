package com.dbdev.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页 Controller
 */
@RestController
@RequiredArgsConstructor
public class IndexController extends BaseController {

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "DB Dev - Database Development Assistant");
        info.put("version", "1.0.0-SNAPSHOT");
        info.put("status", "running");
        return info;
    }
}
