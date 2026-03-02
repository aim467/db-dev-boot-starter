<template>
  <div class="pool-stats-container">
    <!-- 空状态 -->
    <el-empty
      v-if="!loading && (!poolStatsList || poolStatsList.length === 0)"
      description="暂无数据源"
      :image-size="120"
    >
      <template #description>
        <p>暂无数据源信息</p>
        <p class="empty-tip">请检查 Druid 数据源配置</p>
      </template>
    </el-empty>

    <!-- 数据源选择器 + 详情 -->
    <template v-else>
      <!-- 数据源选择标签 -->
      <div class="datasource-selector">
        <div class="selector-label">
          <el-icon><Connection /></el-icon>
          <span>数据源选择</span>
          <el-tag size="small" type="info" class="count-tag">{{ poolStatsList.length }} 个</el-tag>
        </div>
        <el-tabs
          v-model="activeDataSource"
          type="card"
          class="datasource-tabs"
          @tab-change="handleTabChange"
        >
          <el-tab-pane
            v-for="(ds, index) in poolStatsList"
            :key="ds.Identity || index"
            :name="String(ds.Identity || index)"
          >
            <template #label>
              <div class="datasource-tab-label">
                <el-icon><DataLine /></el-icon>
                <span class="ds-name">{{ ds.Name || `数据源 ${index + 1}` }}</span>
                <el-tag
                  size="small"
                  :type="getDbTypeTagType(ds.DbType)"
                  class="db-type-tag"
                >
                  {{ ds.DbType || 'Unknown' }}
                </el-tag>
              </div>
            </template>
          </el-tab-pane>
        </el-tabs>
        <el-button
          type="primary"
          size="small"
          @click="handleRefresh"
          :loading="loading"
          class="refresh-btn"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 选中的数据源详情 -->
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
          <el-card v-if="currentDataSource" class="pool-detail-card" shadow="hover">
            <!-- 数据源头部信息 -->
            <div class="datasource-header">
              <div class="header-main">
                <div class="header-icon">
                  <el-icon><DataLine /></el-icon>
                </div>
                <div class="header-info">
                  <h3 class="ds-title">{{ currentDataSource.Name || '未命名数据源' }}</h3>
                  <div class="ds-meta">
                    <el-tag size="small" :type="getDbTypeTagType(currentDataSource.DbType)">
                      {{ currentDataSource.DbType || 'Unknown' }}
                    </el-tag>
                    <span class="driver-name">{{ currentDataSource.DriverClassName?.split('.').pop() || 'Unknown Driver' }}</span>
                  </div>
                </div>
              </div>
              <div class="header-status">
                <div class="status-item">
                  <span class="status-label">活跃连接</span>
                  <span class="status-value active">{{ currentDataSource.ActiveCount || 0 }}</span>
                </div>
                <div class="status-divider"></div>
                <div class="status-item">
                  <span class="status-label">空闲连接</span>
                  <span class="status-value">{{ currentDataSource.PoolingCount || 0 }}</span>
                </div>
                <div class="status-divider"></div>
                <div class="status-item">
                  <span class="status-label">使用率</span>
                  <span class="status-value" :class="getUsageClass(getUsageRate(currentDataSource))">
                    {{ getUsageRate(currentDataSource) }}%
                  </span>
                </div>
              </div>
            </div>

            <!-- 连接 URL -->
            <div class="url-section">
              <el-icon><Link /></el-icon>
              <el-tooltip :content="currentDataSource.URL || '-'" placement="top">
                <span class="url-text">{{ currentDataSource.URL || '-' }}</span>
              </el-tooltip>
            </div>

            <el-divider />

            <!-- 核心指标卡片 -->
            <div class="metrics-section">
              <h4 class="section-title">
                <el-icon><Odometer /></el-icon>
                实时状态
              </h4>
              <el-row :gutter="16">
                <el-col :xs="12" :sm="6">
                  <div class="metric-card active">
                    <div class="metric-icon">
                      <el-icon><CircleCheck /></el-icon>
                    </div>
                    <div class="metric-content">
                      <span class="metric-value">{{ currentDataSource.ActiveCount || 0 }}</span>
                      <span class="metric-label">活跃连接</span>
                    </div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="6">
                  <div class="metric-card idle">
                    <div class="metric-icon">
                      <el-icon><Timer /></el-icon>
                    </div>
                    <div class="metric-content">
                      <span class="metric-value">{{ currentDataSource.PoolingCount || 0 }}</span>
                      <span class="metric-label">空闲连接</span>
                    </div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="6">
                  <div class="metric-card waiting" :class="{ warning: (currentDataSource.WaitThreadCount || 0) > 0 }">
                    <div class="metric-icon">
                      <el-icon><Loading /></el-icon>
                    </div>
                    <div class="metric-content">
                      <span class="metric-value">{{ currentDataSource.WaitThreadCount || 0 }}</span>
                      <span class="metric-label">等待线程</span>
                    </div>
                  </div>
                </el-col>
                <el-col :xs="12" :sm="6">
                  <div class="metric-card error" :class="{ danger: (currentDataSource.LogicConnectErrorCount || 0) > 0 }">
                    <div class="metric-icon">
                      <el-icon><Warning /></el-icon>
                    </div>
                    <div class="metric-content">
                      <span class="metric-value">{{ currentDataSource.LogicConnectErrorCount || 0 }}</span>
                      <span class="metric-label">连接错误</span>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- 使用率进度条 -->
            <div class="usage-section">
              <div class="usage-header">
                <h4 class="section-title">
                  <el-icon><Histogram /></el-icon>
                  连接池使用率
                </h4>
                <span class="usage-detail">
                  {{ currentDataSource.ActiveCount || 0 }} / {{ currentDataSource.MaxActive || 0 }} 连接
                </span>
              </div>
              <el-progress
                :percentage="getUsageRate(currentDataSource)"
                :color="getProgressColor(getUsageRate(currentDataSource))"
                :stroke-width="16"
                :show-text="false"
                class="usage-progress"
              />
              <div class="usage-legend">
                <div class="legend-item">
                  <span class="legend-dot active"></span>
                  <span>活跃 ({{ currentDataSource.ActiveCount || 0 }})</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot idle"></span>
                  <span>空闲 ({{ currentDataSource.PoolingCount || 0 }})</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot available"></span>
                  <span>可用 ({{ getAvailableCount(currentDataSource) }})</span>
                </div>
              </div>
            </div>

            <!-- 连接池配置 -->
            <div class="config-section">
              <h4 class="section-title">
                <el-icon><Setting /></el-icon>
                连接池配置
              </h4>
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item label="初始连接数">
                  {{ currentDataSource.InitialSize || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="最小空闲">
                  {{ currentDataSource.MinIdle || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="最大连接数">
                  {{ currentDataSource.MaxActive || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="最大等待时间">
                  {{ currentDataSource.MaxWait || 0 }} ms
                </el-descriptions-item>
                <el-descriptions-item label="用户名">
                  {{ currentDataSource.UserName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="是否活跃">
                  <el-tag :type="currentDataSource.KeepAlive ? 'success' : 'info'" size="small">
                    {{ currentDataSource.KeepAlive ? '是' : '否' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <!-- 累计统计 -->
            <div class="cumulative-section">
              <h4 class="section-title">
                <el-icon><TrendCharts /></el-icon>
                累计统计
              </h4>
              <el-row :gutter="16">
                <el-col :xs="12" :sm="6" :md="4" v-for="stat in cumulativeStats" :key="stat.key">
                  <div class="cumulative-item">
                    <span class="cumulative-label">{{ stat.label }}</span>
                    <span class="cumulative-value" :class="stat.class">
                      {{ formatNumber(currentDataSource[stat.key] || 0) }}
                    </span>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </template>
      </el-skeleton>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  poolStatsList: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh', 'change'])

// 当前选中的数据源
const activeDataSource = ref('')

// 当前数据源对象
const currentDataSource = computed(() => {
  if (!props.poolStatsList || props.poolStatsList.length === 0) return null
  const ds = props.poolStatsList.find(
    (item, index) => String(item.Identity || index) === activeDataSource.value
  )
  return ds || props.poolStatsList[0]
})

// 监听数据变化，自动选中第一个
watch(() => props.poolStatsList, (newList) => {
  if (newList && newList.length > 0) {
    const firstId = String(newList[0].Identity || 0)
    if (!activeDataSource.value || !newList.find(item => String(item.Identity) === activeDataSource.value)) {
      activeDataSource.value = firstId
    }
  }
}, { immediate: true })

// 累计统计数据配置
const cumulativeStats = [
  { key: 'LogicConnectCount', label: '逻辑连接', class: '' },
  { key: 'LogicCloseCount', label: '逻辑关闭', class: '' },
  { key: 'PhysicalConnectCount', label: '物理连接', class: '' },
  { key: 'PhysicalCloseCount', label: '物理关闭', class: '' },
  { key: 'LogicConnectErrorCount', label: '连接错误', class: 'error' },
  { key: 'ExecuteCount', label: '执行次数', class: '' }
]

// 处理标签切换
const handleTabChange = (tabName) => {
  activeDataSource.value = tabName
  emit('change', currentDataSource.value)
}

// 刷新
const handleRefresh = () => {
  emit('refresh')
  ElMessage.success('刷新成功')
}

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toLocaleString()
}

// 获取使用率
const getUsageRate = (ds) => {
  if (!ds || !ds.MaxActive || ds.MaxActive === 0) return 0
  const rate = Math.round(((ds.ActiveCount || 0) / ds.MaxActive) * 100)
  return Math.min(rate, 100)
}

// 获取可用连接数
const getAvailableCount = (ds) => {
  if (!ds) return 0
  return (ds.MaxActive || 0) - (ds.ActiveCount || 0)
}

// 获取使用率样式类
const getUsageClass = (rate) => {
  if (rate === undefined || rate === null) return ''
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

// 获取数据库类型标签样式
const getDbTypeTagType = (dbType) => {
  const typeMap = {
    'mysql': 'success',
    'postgresql': 'primary',
    'oracle': 'warning',
    'sqlserver': 'info',
    'h2': 'danger',
    'sqlite': 'info'
  }
  return typeMap[dbType?.toLowerCase()] || 'info'
}
</script>

<style scoped>
.pool-stats-container {
  padding: 0;
}

/* 空状态样式 */
.empty-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

/* 数据源选择器 */
.datasource-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
  flex-wrap: wrap;
}

.selector-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
}

.count-tag {
  margin-left: 4px;
}

.datasource-tabs {
  flex: 1;
  min-width: 200px;
}

.datasource-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.datasource-tabs :deep(.el-tabs__nav) {
  border-radius: 8px;
}

.datasource-tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
}

.ds-name {
  font-weight: 500;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.db-type-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 18px;
  line-height: 16px;
}

.refresh-btn {
  white-space: nowrap;
}

/* 详情卡片 */
.pool-detail-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 数据源头部 */
.datasource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409eff 0%, #1677ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ds-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.ds-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.driver-name {
  font-size: 13px;
  color: #909399;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 24px;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.status-label {
  font-size: 12px;
  color: #909399;
}

.status-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.status-value.active {
  color: #409eff;
}

.status-value.success {
  color: #67c23a;
}

.status-value.warning {
  color: #e6a23c;
}

.status-value.danger {
  color: #f56c6c;
}

.status-divider {
  width: 1px;
  height: 40px;
  background: #e4e7ed;
}

/* URL 区域 */
.url-section {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
  font-size: 13px;
}

.url-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: monospace;
}

/* 区块标题 */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

/* 指标卡片 */
.metrics-section {
  margin: 20px 0;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #f5f7fa;
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.metric-card.active {
  background: linear-gradient(135deg, #ecf5ff 0%, #e6f2ff 100%);
  border: 1px solid #d9ecff;
}

.metric-card.idle {
  background: linear-gradient(135deg, #f0f9eb 0%, #e8f5e0 100%);
  border: 1px solid #e1f3d8;
}

.metric-card.waiting {
  background: linear-gradient(135deg, #fdf6ec 0%, #f9f0d9 100%);
  border: 1px solid #faecd8;
}

.metric-card.waiting.warning {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  border: 1px solid #fcd3d3;
}

.metric-card.error {
  background: linear-gradient(135deg, #f4f4f5 0%, #e9e9eb 100%);
  border: 1px solid #e4e4e6;
}

.metric-card.error.danger {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  border: 1px solid #fcd3d3;
}

.metric-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #606266;
}

.metric-card.active .metric-icon {
  color: #409eff;
}

.metric-card.idle .metric-icon {
  color: #67c23a;
}

.metric-card.waiting .metric-icon {
  color: #e6a23c;
}

.metric-card.waiting.warning .metric-icon {
  color: #f56c6c;
}

.metric-card.error.danger .metric-icon {
  color: #f56c6c;
}

.metric-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.metric-label {
  font-size: 12px;
  color: #909399;
}

/* 使用率区域 */
.usage-section {
  margin: 24px 0;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  border-radius: 12px;
}

.usage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.usage-header .section-title {
  margin: 0;
}

.usage-detail {
  font-size: 13px;
  color: #909399;
}

.usage-progress {
  margin-bottom: 12px;
}

.usage-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-dot.active {
  background: #409eff;
}

.legend-dot.idle {
  background: #67c23a;
}

.legend-dot.available {
  background: #e4e7ed;
}

/* 配置区域 */
.config-section {
  margin: 24px 0;
}

/* 累计统计 */
.cumulative-section {
  margin-top: 24px;
}

.cumulative-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 8px;
  background: #f5f7fa;
  border-radius: 10px;
  transition: all 0.3s;
}

.cumulative-item:hover {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.cumulative-label {
  font-size: 12px;
  color: #909399;
}

.cumulative-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.cumulative-value.error {
  color: #f56c6c;
}

/* Skeleton 样式 */
.skeleton-container {
  padding: 20px;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .datasource-selector {
    flex-direction: column;
    align-items: stretch;
  }

  .selector-label {
    justify-content: center;
  }

  .refresh-btn {
    width: 100%;
  }

  .datasource-header {
    flex-direction: column;
    text-align: center;
  }

  .header-status {
    width: 100%;
    justify-content: space-around;
  }

  .status-divider {
    display: none;
  }

  .skeleton-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .usage-legend {
    flex-wrap: wrap;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .ds-name {
    max-width: 80px;
  }

  .db-type-tag {
    display: none;
  }

  .metric-card {
    flex-direction: column;
    text-align: center;
    padding: 12px;
  }

  .metric-value {
    font-size: 20px;
  }

  .cumulative-item {
    padding: 12px 4px;
  }

  .cumulative-value {
    font-size: 16px;
  }
}
</style>
