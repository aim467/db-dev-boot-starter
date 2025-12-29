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
- ⚡ **SQL 执行器** - 在线执行 SQL 并查看结果（即将推出）
- 🔧 **代码生成** - 基于表结构生成 Entity、Mapper 等代码（即将推出）
- 🎨 **现代化 UI** - 基于 Vue 3 的响应式 Web 界面
- 🔒 **安全优先** - 默认只读模式，支持访问控制

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
- 📊 **Dashboard** - 快速概览
- 🔌 **数据源管理** - 查看所有数据源和连接状态
- 📋 **表结构浏览** - 浏览数据库表、字段、索引等元数据
- ⚡ **SQL 执行器** - 即将推出
- 🔧 **代码生成** - 即将推出

### 4. API 访问

也可以直接访问 REST API：
- `http://localhost:8080/db-dev/api/datasource/list` - 数据源列表
- `http://localhost:8080/db-dev/api/metadata/tables?dataSourceName=dataSource` - 表列表
- `http://localhost:8080/db-dev/api/metadata/table/{tableName}?dataSourceName=dataSource` - 表详情

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
├── db-dev-core             # 核心模块：数据源、元数据、SQL 引擎
├── db-dev-autoconfigure    # 自动配置模块
├── db-dev-web              # Web API 层
├── db-dev-codegen          # 代码生成模块
├── db-dev-starter          # Starter 聚合模块
└── example                 # 示例项目
```

## 🛠️ 技术栈

**后端**
- Spring Boot 3.2.0
- Java 17+
- JDBC / DataSource
- Maven

**前端**
- Vue 3
- TypeScript
- Ant Design Vue
- Axios

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
- [ ] **Phase 2** - SQL 执行器、结果展示
- [ ] **Phase 3** - 代码生成器、模板引擎
- [ ] **Phase 4** - AI 辅助、性能分析

查看 [MVP-PHASE1-COMPLETED.md](MVP-PHASE1-COMPLETED.md) 了解已完成功能

## ⚠️ 注意事项

- 本工具主要用于**开发环境**，不建议在生产环境启用
- 默认配置为只读模式，确保数据安全
- 建议配置访问控制，限制访问 IP

## 📄 License

本项目采用 Apache 2.0 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

<p align="center">
  Made with ❤️ by DB Dev Team
</p>
