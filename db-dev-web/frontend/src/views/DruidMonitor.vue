<template>
  <div class="druid-monitor">
    <!-- 未启用 Druid 提示 -->
    <el-alert
      v-if="!druidEnabled && !loading"
      title="Druid 未启用"
      type="info"
      description="当前项目未使用 Alibaba Druid 数据源，此功能不可用。如需使用，请在项目中引入 druid-spring-boot-3-starter 依赖。"
      show-icon
      :closable="false"
    />

    <!-- Druid 监控内容 -->
    <template v-else-if="druidEnabled">
      <!-- 连接池状态卡片 -->
      <el-card class="pool-stats-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>
              <el-icon><Connection /></el-icon>
              连接池状态
            </span>
            <el-button type="primary" size="small" @click="refreshStats" :loading="refreshing">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <el-row :gutter="20" v-if="poolStats">
          <!-- 基本信息 -->
          <el-col :span="24">
            <div class="info-section">
              <h4>数据源信息</h4>
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item label="名称">{{ poolStats.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="数据库类型">{{ poolStats.dbType || '-' }}</el-descriptions-item>
                <el-descriptions-item label="驱动">{{ poolStats.driverClassName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="URL" :span="3">{{ poolStats.url || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>

          <!-- 连接池配置 -->
          <el-col :span="12">
            <div class="stats-section">
              <h4>连接池配置</h4>
              <div class="stats-grid">
                <div class="stat-item">
                  <span class="label">初始连接数</span>
                  <span class="value">{{ poolStats.initialSize }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">最小空闲</span>
                  <span class="value">{{ poolStats.minIdle }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">最大连接数</span>
                  <span class="value">{{ poolStats.maxActive }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">最大等待时间</span>
                  <span class="value">{{ poolStats.maxWait }}ms</span>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 实时状态 -->
          <el-col :span="12">
            <div class="stats-section">
              <h4>实时状态</h4>
              <div class="stats-grid">
                <div class="stat-item highlight">
                  <span class="label">活跃连接</span>
                  <span class="value active">{{ poolStats.activeCount }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">空闲连接</span>
                  <span class="value">{{ poolStats.poolingCount }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">等待线程</span>
                  <span class="value" :class="{ warning: poolStats.waitThreadCount > 0 }">
                    {{ poolStats.waitThreadCount }}
                  </span>
                </div>
                <div class="stat-item">
                  <span class="label">使用率</span>
                  <span class="value" :class="getUsageClass(poolStats.usageRate)">
                    {{ poolStats.usageRate }}%
                  </span>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 使用率进度条 -->
          <el-col :span="24">
            <div class="usage-bar-section">
              <h4>连接池使用率</h4>
              <el-progress 
                :percentage="poolStats.usageRate" 
                :color="getProgressColor(poolStats.usageRate)"
                :stroke-width="20"
                :format="(p) => `${p}% (${poolStats.activeCount}/${poolStats.maxActive})`"
              />
            </div>
          </el-col>

          <!-- 累计统计 -->
          <el-col :span="24">
            <div class="stats-section">
              <h4>累计统计</h4>
              <div class="stats-grid wide">
                <div class="stat-item">
                  <span class="label">连接次数</span>
                  <span class="value">{{ formatNumber(poolStats.connectCount) }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">关闭次数</span>
                  <span class="value">{{ formatNumber(poolStats.closeCount) }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">创建次数</span>
                  <span class="value">{{ formatNumber(poolStats.createCount) }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">销毁次数</span>
                  <span class="value">{{ formatNumber(poolStats.destroyCount) }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">连接错误</span>
                  <span class="value" :class="{ error: poolStats.connectErrorCount > 0 }">
                    {{ formatNumber(poolStats.connectErrorCount) }}
                  </span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- SQL 统计卡片 -->
      <el-card class="sql-stats-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>
              <el-icon><Document /></el-icon>
              SQL 统计
            </span>
            <div>
              <el-button type="warning" size="small" @click="handleResetStats" :loading="resetting">
                <el-icon><Delete /></el-icon>
                重置统计
              </el-button>
            </div>
          </div>
        </template>

        <el-table :data="sqlStats" stripe style="width: 100%" max-height="400" v-loading="sqlLoading">
          <el-table-column prop="sql" label="SQL" min-width="300" show-overflow-tooltip />
          <el-table-column prop="executeCount" label="执行次数" width="100" sortable>
            <template #default="{ row }">
              {{ formatNumber(row.executeCount) }}
            </template>
          </el-table-column>
          <el-table-column prop="totalTime" label="总耗时(ms)" width="110" sortable>
            <template #default="{ row }">
              {{ formatNumber(row.totalTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="avgTime" label="平均耗时(ms)" width="120" sortable>
            <template #default="{ row }">
              <span :class="{ 'slow-query': row.avgTime > 1000 }">
                {{ row.avgTime }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="maxTime" label="最大耗时(ms)" width="120" sortable>
            <template #default="{ row }">
              <span :class="{ 'slow-query': row.maxTime > 3000 }">
                {{ formatNumber(row.maxTime) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="errorCount" label="错误次数" width="100" sortable>
            <template #default="{ row }">
              <span :class="{ error: row.errorCount > 0 }">
                {{ row.errorCount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="runningCount" label="执行中" width="80" />
        </el-table>

        <div v-if="sqlStats.length === 0 && !sqlLoading" class="empty-tip">
          暂无 SQL 统计数据
        </div>
      </el-card>
    </template>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkDruidEnabled, getPoolStats, getSqlStats, resetStats } from '@/api/druid'

const loading = ref(true)
const druidEnabled = ref(false)
const poolStats = ref(null)
const sqlStats = ref([])
const refreshing = ref(false)
const sqlLoading = ref(false)
const resetting = ref(false)

let refreshTimer = null

// 检查 Druid 是否启用
const checkEnabled = async () => {
  try {
    const res = await checkDruidEnabled()
    druidEnabled.value = res.data?.enabled || false
    if (druidEnabled.value) {
      await loadAllStats()
    }
  } catch (error) {
    druidEnabled.value = false
  } finally {
    loading.value = false
  }
}

// 加载所有统计数据
const loadAllStats = async () => {
  await Promise.all([loadPoolStats(), loadSqlStats()])
}

// 加载连接池状态
const loadPoolStats = async () => {
  try {
    const res = await getPoolStats()
    poolStats.value = res.data
  } catch (error) {
    console.error('Failed to load pool stats:', error)
  }
}

// 加载 SQL 统计
const loadSqlStats = async () => {
  sqlLoading.value = true
  try {
    const res = await getSqlStats()
    sqlStats.value = res.data || []
  } catch (error) {
    console.error('Failed to load SQL stats:', error)
  } finally {
    sqlLoading.value = false
  }
}

// 刷新统计
const refreshStats = async () => {
  refreshing.value = true
  try {
    await loadAllStats()
    ElMessage.success('刷新成功')
  } finally {
    refreshing.value = false
  }
}

// 重置统计
const handleResetStats = async () => {
  try {
    await ElMessageBox.confirm('确定要重置所有统计数据吗？此操作不可恢复。', '确认重置', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    resetting.value = true
    await resetStats()
    ElMessage.success('统计数据已重置')
    await loadAllStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  } finally {
    resetting.value = false
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return '-'
  return num.toLocaleString()
}

// 获取使用率样式类
const getUsageClass = (rate) => {
  if (rate >= 80) return 'danger'
  if (rate >= 60) return 'warning'
  return 'success'
}

// 获取进度条颜色
const getProgressColor = (rate) => {
  if (rate >= 80) return '#f56c6c'
  if (rate >= 60) return '#e6a23c'
  return '#67c23a'
}

onMounted(() => {
  checkEnabled()
  // 每 10 秒自动刷新
  refreshTimer = setInterval(() => {
    if (druidEnabled.value) {
      loadPoolStats()
    }
  }, 10000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.druid-monitor {
  padding: 10px;
}

.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.el-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.pool-stats-card,
.sql-stats-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.info-section,
.stats-section,
.usage-bar-section {
  margin-bottom: 20px;
}

.info-section h4,
.stats-section h4,
.usage-bar-section h4 {
  margin-bottom: 12px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-section h4::before,
.stats-section h4::before,
.usage-bar-section h4::before {
  content: '';
  width: 3px;
  height: 14px;
  background: #409eff;
  border-radius: 2px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stats-grid.wide {
  grid-template-columns: repeat(5, 1fr);
}

.stat-item {
  background: #f5f7fa;
  padding: 20px 16px;
  border-radius: 12px;
  text-align: center;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.stat-item:hover {
  background: #fff;
  border-color: #409eff33;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.stat-item.highlight {
  background: linear-gradient(135deg, #409eff0d 0%, #67c23a0d 100%);
  border-color: #409eff1a;
}

.stat-item .label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-item .value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-item .value.active {
  color: #409eff;
}

.stat-item .value.success {
  color: #67c23a;
}

.stat-item .value.warning {
  color: #e6a23c;
}

.stat-item .value.danger,
.stat-item .value.error {
  color: #f56c6c;
}

.usage-bar-section {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  border-radius: 12px;
}

.usage-bar-section h4 {
  margin-bottom: 16px;
}

.usage-bar-section .el-progress {
  margin-top: 8px;
}

.slow-query {
  color: #e6a23c;
  font-weight: 600;
}

.error {
  color: #f56c6c;
  font-weight: 600;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #909399;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-tip .el-icon {
  font-size: 48px;
  color: #c0c4cc;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #909399;
}

.loading-container .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

/* 表格样式优化 */
.sql-stats-card .el-table {
  margin-top: 16px;
}

.sql-stats-card .el-table th {
  background: #f5f7fa !important;
}

@media (max-width: 768px) {
  .stats-grid.wide {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-item {
    padding: 16px 12px;
  }

  .stat-item .value {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid.wide {
    grid-template-columns: 1fr;
  }
}
</style>
