package com.dbdev.codegen.controller;

import com.dbdev.codegen.model.CodeGenConfig;
import com.dbdev.codegen.service.CodeGenService;
import com.dbdev.core.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 代码生成控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/codegen")
public class CodeGenController {

    private final CodeGenService codeGenService;

    /**
     * 生成代码
     */
    @PostMapping("/generate")
    public Result<Map<String, String>> generate(@RequestBody CodeGenConfig config) {
        try {
            Map<String, String> generatedFiles = codeGenService.generateCode(config);
            Result<Map<String, String>> result = Result.success(generatedFiles);
            result.setMessage("代码生成成功");
            return result;
        } catch (IllegalArgumentException e) {
            Result<Map<String, String>> result = new Result<>();
            result.setCode(400);
            result.setMessage(e.getMessage());
            return result;
        } catch (Exception e) {
            Result<Map<String, String>> result = new Result<>();
            result.setCode(500);
            result.setMessage("代码生成失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 预览代码（不保存文件）
     */
    @PostMapping("/preview")
    public Result<Map<String, String>> preview(@RequestBody CodeGenConfig config) {
        try {
            // 设置不输出文件
            config.setOutputDir(null);
            Map<String, String> generatedFiles = codeGenService.generateCode(config);
            Result<Map<String, String>> result = Result.success(generatedFiles);
            result.setMessage("代码预览成功");
            return result;
        } catch (IllegalArgumentException e) {
            Result<Map<String, String>> result = new Result<>();
            result.setCode(400);
            result.setMessage(e.getMessage());
            return result;
        } catch (Exception e) {
            Result<Map<String, String>> result = new Result<>();
            result.setCode(500);
            result.setMessage("代码预览失败: " + e.getMessage());
            return result;
        }
    }
}