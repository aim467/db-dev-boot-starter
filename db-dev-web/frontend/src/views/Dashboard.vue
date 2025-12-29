<template>
  <div>
    <el-card class="welcome-card" shadow="hover">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon color="#fff"><Promotion /></el-icon>
          <span style="font-weight: 600; color: #fff;">欢迎使用 DB Dev</span>
        </div>
      </template>
      <p style="margin-bottom: 15px; color: #fff; font-size: 14px; line-height: 1.6;">
        这是一个面向开发者的数据库开发辅助工具，提供以下功能：
      </p>
      <ul class="feature-list">
        <li><el-icon color="#67c23a"><Connection /></el-icon> <span>数据源管理与监控</span></li>
        <li><el-icon color="#409eff"><Folder /></el-icon> <span>数据库元数据浏览</span></li>
        <li><el-icon color="#e6a23c"><Lightning /></el-icon> <span>SQL 执行与调试</span></li>
        <li><el-icon color="#f56c6c"><Tools /></el-icon> <span>代码生成工具（即将推出）</span></li>
      </ul>
    </el-card>
    
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon color="#67c23a"><Guide /></el-icon>
              <span style="font-weight: 600;">快速开始</span>
            </div>
          </template>
          <el-alert
            title="点击左侧菜单开始探索您的数据库"
            type="info"
            :closable="false"
            show-icon>
          </el-alert>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon color="#409eff"><InfoFilled /></el-icon>
              <span style="font-weight: 600;">系统信息</span>
            </div>
          </template>
          <div style="line-height: 2;">
            <p><el-icon size="14"><Clock /></el-icon> 版本: 1.0.0</p>
            <p><el-icon size="14"><User /></el-icon> 用户: Developer</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
</script>

<style scoped>
.welcome-card {
  margin-bottom: 24px;
  border: none;
  border-radius: 16px;
  background-color: #3b98ff !important;
  color: white;
  overflow: hidden;
  position: relative;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

:deep(.welcome-card .el-card__header) {
  background: rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
}

:deep(.welcome-card .el-card__body) {
  position: relative;
  z-index: 1;
}

.feature-list {
  margin-top: 16px;
  line-height: 2.2;
  list-style: none;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  transition: all 0.3s ease;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  margin-bottom: 10px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(5px);
}

.feature-list li:hover {
  transform: translateX(8px);
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.feature-list li .el-icon {
  font-size: 22px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
  flex-shrink: 0;
}

.feature-list li span {
  color: #fff;
}

.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}
</style>
