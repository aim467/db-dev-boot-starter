<template>
  <el-card class="pool-stats-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>
          <el-icon><Connection /></el-icon>
          连接池状态
        </span>
        <el-button type="primary" size="small" @click="handleRefresh" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </template>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-container">
          <el-skeleton-item variant="h3" style="width: 200px" />
          <div class="skeleton-grid">
            <el-skeleton-item v-for="i in 8" :key="i" variant="rect" style="width: 100%; height: 80px" />
          </div>
        </div>
      </template>
      
      <template #default>
        <el-row :gutter="20" v-if="poolStats">
          <!-- 基本信息 -->
          <el-col :span="24">
            <div class="info-section">
              <h4>数据源信息</h4>
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item label="名称">{{ poolStats.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="数据库类型">{{ poolStats.dbType || '-' }}</el-descriptions-item>
                <el-descriptions-item label="驱动">{{ poolStats.driverClassName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="URL" :span="3">
                  <el-tooltip :content="poolStats.url || '-'" placement="top">
                    <span class="url-text">{{ poolStats.url || '-' }}</span>
                  </el-tooltip>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>

          <!-- 连接池配置 -->
          <el-col :xs="24" :sm="12">
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
                  <span class="label">最大等待</span>
                  <span class="value">{{ poolStats.maxWait }}ms</span>
                </div>
              </div>
            </div>
          </el-col>

          <!-- 实时状态 -->
          <el-col :xs="24" :sm="12">
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
                  <span class="value" :class="getWaitClass(poolStats.waitThreadCount)">
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
              <div class="usage-info">
                <span class="usage-text">
                  {{ poolStats.activeCount }} / {{ poolStats.maxActive }} 连接
                </span>
                <span class="usage-rate" :class="getUsageClass(poolStats.usageRate)">
                  {{ poolStats.usageRate }}%
                </span>
              </div>
              <el-progress 
                :percentage="poolStats.usageRate" 
                :color="getProgressColor(poolStats.usageRate)"
                :stroke-width="20"
                :show-text="false"
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

          <!-- 连接池详情图表 -->
          <el-col :span="24">
            <div class="chart-section">
              <h4>连接状态分布</h4>
              <div class="pie-chart-container">
                <div class="pie-chart">
                  <div class="pie-segment active" :style="getPieStyle('active')">
                  </div>
                  <div class="pie-segment idle" :style="getPieStyle('idle')">
                  </div>
                  <div class="pie-segment wait" :style="getPieStyle('wait')">
                  </div>
                </div>
                <div class="pie-legend">
                  <div class="legend-item">
                    <span class="legend-color active"></span>
                    <span>活跃连接 ({{ poolStats.activeCount }})</span>
                  </div>
                  <div class="legend-item">
                    <span class="legend-color idle"></span>
                    <span>空闲连接 ({{ poolStats.poolingCount }})</span>
                  </div>
                  <div class="legend-item">
                    <span class="legend-color wait"></span>
                    <span>等待线程 ({{ poolStats.waitThreadCount }})</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>
    </el-skeleton>
  </el-card>
</template>

<script setup>
import { ElMessage } from 'element-plus'

const props = defineProps({
  poolStats: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh'])

// 刷新
const handleRefresh = () => {
  emit('refresh')
  ElMessage.success('刷新成功')
}

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

// 获取使用率样式类
const getUsageClass = (rate) => {
  if (rate === undefined || rate === null) return ''
  if (rate >= 80) return 'danger'
  if (rate >= 60) return 'warning'
  return 'success'
}

// 获取等待线程样式类
const getWaitClass = (count) => {
  if (count > 0) return 'warning'
  return ''
}

// 获取进度条颜色
const getProgressColor = (rate) => {
  if (rate >= 80) return '#f56c6c'
  if (rate >= 60) return '#e6a23c'
  return '#67c23a'
}

// 获取饼图样式
const getPieStyle = (type) => {
  const total = (props.poolStats?.activeCount || 0) + 
                (props.poolStats?.poolingCount || 0) + 
                (props.poolStats?.waitThreadCount || 0)
  
  if (total === 0) return {}
  
  const activeAngle = ((props.poolStats?.activeCount || 0) / total) * 360
  const idleAngle = ((props.poolStats?.poolingCount || 0) / total) * 360
  
  if (type === 'active') {
    return { '--end-angle': `${activeAngle}deg` }
  } else if (type === 'idle') {
    return { 
      '--start-angle': `${activeAngle}deg`,
      '--end-angle': `${activeAngle + idleAngle}deg`
    }
  } else {
    return { '--start-angle': `${activeAngle + idleAngle}deg` }
  }
}
</script>

<style scoped>
.pool-stats-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.pool-stats-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
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
.usage-bar-section,
.chart-section {
  margin-bottom: 20px;
}

.info-section h4,
.stats-section h4,
.usage-bar-section h4,
.chart-section h4 {
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
.usage-bar-section h4::before,
.chart-section h4::before {
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

.url-text {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.usage-bar-section {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  border-radius: 12px;
}

.usage-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.usage-text {
  font-size: 14px;
  color: #606266;
}

.usage-rate {
  font-size: 24px;
  font-weight: 700;
}

.usage-rate.success {
  color: #67c23a;
}

.usage-rate.warning {
  color: #e6a23c;
}

.usage-rate.danger {
  color: #f56c6c;
}

.chart-section {
  margin-top: 20px;
}

.pie-chart-container {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
}

.pie-chart {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: conic-gradient(
    #409eff 0deg 120deg,
    #67c23a 120deg 240deg,
    #e6a23c 240deg 360deg
  );
  position: relative;
  flex-shrink: 0;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-color.active {
  background: #409eff;
}

.legend-color.idle {
  background: #67c23a;
}

.legend-color.wait {
  background: #e6a23c;
}

/* Skeleton 样式 */
.skeleton-container {
  padding: 20px;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 20px;
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

  .pie-chart-container {
    flex-direction: column;
    gap: 20px;
  }

  .pie-chart {
    width: 120px;
    height: 120px;
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