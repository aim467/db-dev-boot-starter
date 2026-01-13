package com.dbdev.web.controller;

import com.dbdev.core.service.SchemaExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 数据库表结构导出 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController extends BaseController {

    private final SchemaExportService schemaExportService;

    /**
     * 导出表结构文档
     *
     * @param dataSourceName 数据源名称
     * @param format         导出格式: markdown, html, pdf, sql
     */
    @GetMapping("/{format}")
    public void exportSchema(
            @RequestParam String dataSourceName,
            @PathVariable String format,
            HttpServletResponse response) throws IOException {

        try {
            String content;
            String fileName;
            MediaType mediaType;

            switch (format.toLowerCase()) {
                case "markdown":
                case "md":
                    content = schemaExportService.exportToMarkdown(dataSourceName);
                    fileName = dataSourceName + "_schema_" + System.currentTimeMillis() + ".md";
                    mediaType = MediaType.TEXT_MARKDOWN;
                    break;
                case "html":
                    content = schemaExportService.exportToHtml(dataSourceName);
                    fileName = dataSourceName + "_schema_" + System.currentTimeMillis() + ".html";
                    mediaType = MediaType.TEXT_HTML;
                    break;
                case "sql":
                    content = schemaExportService.exportToSql(dataSourceName);
                    fileName = dataSourceName + "_schema_" + System.currentTimeMillis() + ".sql";
                    mediaType = MediaType.parseMediaType("text/sql; charset=UTF-8");
                    break;
                default:
                    response.setStatus(400);
                    response.getWriter()
                            .write("Unsupported format: " + format + ". Supported formats: markdown, html, sql");
                    return;
            }

            // 设置响应头
            response.setContentType(mediaType.toString());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            response.setCharacterEncoding("UTF-8");

            // 写入内容
            response.getWriter().write(content);
            response.getWriter().flush();

            log.info("导出表结构文档成功: dataSource={}, format={}", dataSourceName, format);

        } catch (SQLException e) {
            log.error("导出表结构失败: dataSource={}, format={}", dataSourceName, format, e);
            response.setStatus(500);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("导出失败: " + e.getMessage());
        }
    }
}