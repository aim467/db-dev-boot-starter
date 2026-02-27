# DB Dev Spring Boot Starter

<p align="center">
  <strong>一个面向开发者的数据库开发辅助 Spring Boot Starter</strong>
</p>

<p align="center">
  在运行时提供数据库可视化、SQL 操作与代码生成能力
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-17+-blue.svg" alt="Java">
  <img src="https://img.shields.io/badge/License-Apache%202.0-orange.svg" alt="License">
  <img src="https://img.shields.io/badge/Version-1.0.0--SNAPSHOT-red.svg" alt="Version">
</p>

---

## ✨ 特性

- 🚀 **即插即用** - 添加依赖即可使用，零侵入式设计
- 📊 **数据源管理** - 实时查看所有数据源和连接池状态
- 🔍 **元数据浏览** - 可视化浏览数据库表、字段、索引等结构
- 📈 **Druid 监控** - 深度集成 Druid 连接池监控，实时查看 SQL/URL 统计和性能分析
- ⚡ **SQL 执行器** - 在线执行 SELECT 查询，支持结果导出、历史记录、SQL 分析
- 🔧 **代码生成器** - 基于表结构生成 Entity、Mapper、XML、Repository 代码
- 📦 **结构导出** - 支持导出数据库表结构为 Markdown、HTML、SQL 格式
- 🎨 **现代化 UI** - 基于 Vue 3 + Element Plus 的响应式 Web 界面
- 🔒 **安全优先** - SQL 执行器仅支持只读查询，保障数据安全

## 快速开始

详细步骤请查看 [QUICK-START.md](QUICK-START.md)

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.dbdev</groupId>
    <artifactId>db-dev-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置启用

```yaml
db:
  dev:
    enabled: true
```

### 3. 访问 Web UI

启动应用后，在浏览器中访问：

```
http://localhost:8080/db-dev
```

Web UI 提供以下功能：
- 📊 **Dashboard** - 系统概览和快速统计
- 🔌 **数据源管理** - 查看所有数据源、连接状态、连接池监控
- 📋 **表结构浏览** - 浏览数据库表、字段、索引，支持导出文档
- 📈 **Druid 监控** - 连接池状态、SQL/URL 执行统计、性能分析
- ⚡ **SQL 执行器** - 在线执行查询、查看结果、历史记录、SQL 分析
- 🔧 **代码生成器** - 可视化配置生成实体类、Mapper、XML 等代码

### 4. API 访问

也可以直接访问 REST API：

**数据源相关**
- `GET /api/datasource/list` - 数据源列表
- `GET /api/datasource/{name}/stats` - 连接池统计

**元数据相关**
- `GET /api/metadata/tables?dataSourceName=xxx` - 表列表
- `GET /api/metadata/table/{tableName}?dataSourceName=xxx` - 表详情

**SQL 执行相关**
- `POST /api/sql/execute` - 执行 SQL 查询
- `POST /api/sql/analyze` - SQL 执行计划分析

**代码生成相关**
- `POST /api/codegen/preview` - 预览生成代码
- `POST /api/codegen/generate` - 生成并下载代码

**Druid 监控相关**
- `GET /api/druid/enabled` - 检查是否启用 Druid 监控
- `GET /api/druid/pool-stats` - 获取连接池状态信息
- `GET /api/druid/sql-stats` - 获取 SQL 执行统计
- `GET /api/druid/url-stats` - 获取 URL 访问统计
- `POST /api/druid/reset-stats` - 重置 Druid 统计数据

**导出相关**
- `GET /api/export/markdown?dataSourceName=xxx` - 导出 Markdown
- `GET /api/export/html?dataSourceName=xxx` - 导出 HTML
- `GET /api/export/sql?dataSourceName=xxx` - 导出 SQL

## 文档

> 📚 查看 [完整文档索引](DOCS-INDEX.md) 了解所有文档

### 快速上手
- [快速开始](QUICK-START.md) - 5 分钟上手指南
- [UI 快速体验](UI-README.md) - Web UI 快速上手
- [功能演示](DEMO.md) - 完整功能演示指南
- [功能验证清单](CHECKLIST.md) - 快速验证功能

### 使用指南
- [UI 使用指南](UI-GUIDE.md) - Web UI 详细使用说明
- [部署说明](UI-DEPLOYMENT.md) - 生产环境部署指南
- [故障排查](TROUBLESHOOTING.md) - 常见问题解决

### 开发文档
- [开发指南](DEVELOPMENT.md) - 开发者文档
- [项目结构](PROJECT-STRUCTURE.md) - 项目架构说明
- [项目规划](project.md) - 完整项目文档

### 版本与变更
- [版本说明](VERSION.md) - 当前版本信息
- [变更记录](CHANGES.md) - 最新变更说明
- [Phase 1 完成报告](MVP-PHASE1-COMPLETED.md) - MVP 阶段总结
- [项目总结](SUMMARY.md) - 完整项目总结

## 🏗️ 项目架构

```
db-dev-spring-boot-starter/
├── db-dev-core             # 核心模块：数据源、元数据、SQL 执行、连接池监控
├── db-dev-autoconfigure    # 自动配置模块
├── db-dev-web              # Web API 层和前端 UI
├── db-dev-codegen          # 代码生成模块（FreeMarker 模板引擎）
├── db-dev-ai               # AI 辅助分析模块（可选）
├── db-dev-starter          # Starter 聚合模块
└── example                 # 示例项目
```

## 🛠️ 技术栈

**后端**
- Spring Boot 3.2.0
- Java 17+
- JDBC / DataSource
- Alibaba Druid（连接池监控）
- FreeMarker（代码生成模板）
- Maven

**前端**
- Vue 3 + Composition API
- Element Plus
- Axios
- Vite

## 📋 系统要求

- JDK 17 或更高版本
- Maven 3.6+
- Spring Boot 3.x 应用

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

详细开发指南请参考 [DEVELOPMENT.md](DEVELOPMENT.md)

## 📝 开发路线

- [x] **Phase 1: MVP** - 数据源管理、元数据浏览、基础 UI ✅
- [x] **Phase 2: SQL 执行** - SQL 执行器、结果展示、历史记录、性能分析 ✅
- [x] **Phase 3: 代码生成** - 代码生成器、FreeMarker 模板引擎、多格式支持 ✅
- [x] **Phase 4: 监控增强** - Druid 连接池监控、SQL 统计、性能优化建议 ✅
- [ ] **Phase 5: AI 增强** - AI 辅助 SQL 分析、智能优化建议（可选）
- [ ] **Phase 6: 扩展功能** - 多数据库支持、访问控制、审计日志

查看 [MVP-PHASE1-COMPLETED.md](MVP-PHASE1-COMPLETED.md) 了解已完成功能

## ⚠️ 注意事项

- 本工具主要用于**开发环境**，不建议在生产环境启用
- SQL 执行器仅支持 SELECT 查询，不允许执行修改操作（INSERT/UPDATE/DELETE）
- 查询结果最多返回 1000 条记录，避免内存溢出
- 建议配合 Druid 连接池使用以获得完整的监控功能
- 代码生成器支持自定义模板，适配不同项目规范
- 导出功能支持 Markdown、HTML、SQL 多种格式

## 📄 License

本项目采用 Apache 2.0 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

<p align="center">
  Made with ❤️ by DB Dev Team
</p>
