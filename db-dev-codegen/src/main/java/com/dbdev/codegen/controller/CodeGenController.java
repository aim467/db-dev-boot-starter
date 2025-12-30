package com.dbdev.codegen.controller;

import com.dbdev.codegen.model.CodeGenConfig;
import com.dbdev.codegen.service.CodeGenService;
import com.dbdev.core.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

/**
 * 代码生成控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/codegen")
public class CodeGenController {

    private final CodeGenService codeGenService;

    @Value("${db.dev.ui-path}")
    private String uiPath;

    /**
     * 生成代码并返回下载链接
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestBody CodeGenConfig config) {
        try {
            // 先生成预览代码
            Map<String, String> generatedFiles = codeGenService.generateCode(config);
            // 生成ZIP包
            String zipFileName = codeGenService.generateAndPackage(config);

            Result<Map<String, Object>> result = Result.success(Map.of(
                    "files", generatedFiles,
                    "downloadUrl", uiPath + "/api/codegen/download/" + zipFileName,
                    "fileName", zipFileName
            ));
            result.setMessage("代码生成成功");
            return result;
        } catch (IllegalArgumentException e) {
            Result<Map<String, Object>> result = new Result<>();
            result.setCode(400);
            result.setMessage(e.getMessage());
            return result;
        } catch (Exception e) {
            Result<Map<String, Object>> result = new Result<>();
            result.setCode(500);
            result.setMessage("代码生成失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 下载生成的代码压缩包
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        try {
            // 安全检查：防止路径遍历攻击
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return ResponseEntity.badRequest().build();
            }

            Path filePath = codeGenService.getZipFilePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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