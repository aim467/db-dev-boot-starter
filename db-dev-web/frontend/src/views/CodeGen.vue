<template>
  <el-card shadow="hover">
    <template #header>
      <div style="display: flex; align-items: center; gap: 8px;">
        <el-icon><Tools /></el-icon>
        <span style="font-weight: 600;">代码生成</span>
      </div>
    </template>
    <el-alert
      title="此功能即将推出"
      type="info"
      description="支持MyBatis、JPA、Spring Data JPA等多种框架的代码生成..."
      :closable="false"
      show-icon>
    </el-alert>
    <div style="margin-top: 20px; padding: 40px; text-align: center; color: #909399;">
      <el-icon size="48"><Clock /></el-icon>
      <p style="margin-top: 16px;">敬请期待</p>
    </div>
  </el-card>
</template>

<script setup>
</script>

<style scoped>
.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}
</style>
