# DB Dev Spring Boot Starter

一个面向开发者的数据库开发辅助 Spring Boot Starter，在运行时提供数据库可视化、SQL 操作与代码生成能力。

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

## License

Apache 2.0
