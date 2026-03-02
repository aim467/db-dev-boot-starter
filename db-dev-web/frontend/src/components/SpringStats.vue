<template>
  <div class="spring-stats">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Monitor /></el-icon>
            Spring 方法监控
          </span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="refresh" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计概览卡片 -->
      <el-row :gutter="16" class="stats-overview">
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card primary">
            <div class="stat-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(totalMethods) }}</div>
              <div class="stat-label">监控方法数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card success">
            <div class="stat-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(totalExecuteCount) }}</div>
              <div class="stat-label">总执行次数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card warning">
            <div class="stat-icon">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(totalExecuteTime) }}<span class="unit">ms</span></div>
              <div class="stat-label">总执行时间</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card info">
            <div class="stat-icon">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(avgExecuteTime) }}<span class="unit">ms</span></div>
              <div class="stat-label">平均执行时间</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card" :class="totalErrorCount > 0 ? 'danger' : 'success'">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value" :class="{ 'text-danger': totalErrorCount > 0 }">
                {{ formatNumber(totalErrorCount) }}
              </div>
              <div class="stat-label">错误次数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card purple">
            <div class="stat-icon">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(totalJdbcExecuteCount) }}</div>
              <div class="stat-label">JDBC 执行次数</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 方法统计表格 -->
      <div class="table-section">
        <div class="section-header">
          <span class="section-title">
            <el-icon><List /></el-icon>
            方法执行详情
            <el-tag v-if="springStats.length > 0" size="small" type="info" class="count-badge">
              {{ springStats.length }}
            </el-tag>
          </span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索类名或方法名"
            size="small"
            clearable
            style="width: 250px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <el-table
          :data="filteredStats"
          stripe
          style="width: 100%"
          max-height="500"
          v-loading="loading"
          :default-sort="{ prop: 'ExecuteCount', order: 'descending' }"
          @expand-change="handleExpandChange"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="expand-detail">
                <!-- JDBC 统计详情 -->
                <el-row :gutter="16" class="detail-row">
                  <el-col :span="24">
                    <h4 class="detail-title">
                      <el-icon><Connection /></el-icon>
                      JDBC 统计
                    </h4>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="6">
                    <div class="detail-item">
                      <span class="detail-label">连接打开:</span>
                      <span class="detail-value">{{ formatNumber(row.JdbcPoolConnectionOpenCount) }}</span>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="6">
                    <div class="detail-item">
                      <span class="detail-label">连接关闭:</span>
                      <span class="detail-value">{{ formatNumber(row.JdbcPoolConnectionCloseCount) }}</span>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="6">
                    <div class="detail-item">
                      <span class="detail-label">ResultSet 打开:</span>
                      <span class="detail-value">{{ formatNumber(row.JdbcResultSetOpenCount) }}</span>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="6">
                    <div class="detail-item">
                      <span class="detail-label">ResultSet 关闭:</span>
                      <span class="detail-value">{{ formatNumber(row.JdbcResultSetCloseCount) }}</span>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="6">
                    <div class="detail-item">
                      <span class="detail-label">提交次数:</span>
                      <span class="detail-value">{{ formatNumber(row.JdbcCommitCount) }}</span>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="6">
                    <div class="detail-item">
                      <span class="detail-label">回滚次数:</span>
                      <span class="detail-value">{{ formatNumber(row.JdbcRollbackCount) }}</span>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="6">
                    <div class="detail-item">
                      <span class="detail-label">获取行数:</span>
                      <span class="detail-value">{{ formatNumber(row.JdbcFetchRowCount) }}</span>
                    </div>
                  </el-col>
                  <el-col :xs="12" :sm="8" :md="6">
                    <div class="detail-item">
                      <span class="detail-label">更新行数:</span>
                      <span class="detail-value">{{ formatNumber(row.JdbcUpdateCount) }}</span>
                    </div>
                  </el-col>
                </el-row>

                <!-- 执行时间直方图 -->
                <el-row :gutter="16" class="detail-row">
                  <el-col :span="24">
                    <h4 class="detail-title">
                      <el-icon><Histogram /></el-icon>
                      执行时间分布
                    </h4>
                    <div class="histogram-container">
                      <div
                        v-for="(value, index) in row.Histogram"
                        :key="index"
                        class="histogram-bar-wrapper"
                      >
                        <div
                          class="histogram-bar"
                          :style="{ height: getHistogramHeight(row.Histogram, value) }"
                          :title="`${getHistogramLabel(index)}: ${value} 次`"
                        />
                        <span class="histogram-label">{{ getHistogramLabel(index) }}</span>
                        <span class="histogram-value">{{ value }}</span>
                      </div>
                    </div>
                  </el-col>
                </el-row>

                <!-- 错误信息 -->
                <el-row v-if="row.LastError" :gutter="16" class="detail-row">
                  <el-col :span="24">
                    <el-alert
                      title="最近错误"
                      type="error"
                      :description="row.LastError"
                      :closable="false"
                      show-icon
                    />
                  </el-col>
                </el-row>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="Class" label="类名" min-width="250" show-overflow-tooltip>
            <template #default="{ row }">
              <el-tag size="small" effect="plain" type="info">
                {{ getSimpleClassName(row.Class) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="Method" label="方法" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="method-name">{{ getMethodName(row.Method) }}</span>
              <span class="method-params">{{ getMethodParams(row.Method) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="ExecuteCount" label="执行次数" width="100" sortable align="center">
            <template #default="{ row }">
              <strong>{{ formatNumber(row.ExecuteCount) }}</strong>
            </template>
          </el-table-column>

          <el-table-column prop="ExecuteErrorCount" label="错误" width="90" sortable align="center">
            <template #default="{ row }">
              <el-tag v-if="row.ExecuteErrorCount > 0" size="small" type="danger">
                {{ formatNumber(row.ExecuteErrorCount) }}
              </el-tag>
              <span v-else class="text-muted">0</span>
            </template>
          </el-table-column>

          <el-table-column prop="ExecuteTimeMillis" label="执行时间" width="110" sortable align="center">
            <template #default="{ row }">
              <el-tag
                size="small"
                :type="getExecuteTimeType(row.ExecuteTimeMillis)"
                effect="light"
              >
                {{ row.ExecuteTimeMillis }}ms
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="ConcurrentMax" label="最大并发" width="100" sortable align="center">
            <template #default="{ row }">
              <span :class="{ 'text-warning': row.ConcurrentMax > 10 }">
                {{ formatNumber(row.ConcurrentMax) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="RunningCount" label="运行中" width="90" sortable align="center">
            <template #default="{ row }">
              <el-tag v-if="row.RunningCount > 0" size="small" type="warning" effect="dark">
                {{ formatNumber(row.RunningCount) }}
              </el-tag>
              <span v-else class="text-muted">0</span>
            </template>
          </el-table-column>

          <el-table-column label="JDBC 执行" width="100" sortable align="center">
            <template #default="{ row }">
              <span class="count-badge jdbc">{{ formatNumber(row.JdbcExecuteCount) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="JDBC 时间" width="100" sortable align="center">
            <template #default="{ row }">
              <span class="text-muted">{{ row.JdbcExecuteTimeMillis }}ms</span>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="filteredStats.length === 0 && !loading" class="empty-tip">
          <el-empty :description="searchKeyword ? '未找到匹配的方法' : '暂无 Spring 监控数据'" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { getSpringStats } from '@/api/druid'
import {
  Monitor,
  Refresh,
  Collection,
  CircleCheck,
  Timer,
  DataLine,
  Warning,
  Connection,
  List,
  Search,
  Histogram
} from '@element-plus/icons-vue'

const props = defineProps({
  autoRefresh: {
    type: Boolean,
    default: false
  },
  refreshInterval: {
    type: Number,
    default: 30000
  }
})

const emit = defineEmits(['loaded'])

const loading = ref(false)
const springStats = ref([])
const searchKeyword = ref('')
let refreshTimer = null

// 计算统计值
const totalMethods = computed(() => springStats.value.length)

const totalExecuteCount = computed(() => {
  return springStats.value.reduce((sum, item) => sum + (item.ExecuteCount || 0), 0)
})

const totalExecuteTime = computed(() => {
  return springStats.value.reduce((sum, item) => sum + (item.ExecuteTimeMillis || 0), 0)
})

const avgExecuteTime = computed(() => {
  const total = totalExecuteCount.value
  return total > 0 ? Math.round(totalExecuteTime.value / total) : 0
})

const totalErrorCount = computed(() => {
  return springStats.value.reduce((sum, item) => sum + (item.ExecuteErrorCount || 0), 0)
})

const totalJdbcExecuteCount = computed(() => {
  return springStats.value.reduce((sum, item) => sum + (item.JdbcExecuteCount || 0), 0)
})

// 过滤后的统计数据
const filteredStats = computed(() => {
  if (!searchKeyword.value) return springStats.value
  const keyword = searchKeyword.value.toLowerCase()
  return springStats.value.filter(item => {
    const classMatch = (item.Class || '').toLowerCase().includes(keyword)
    const methodMatch = (item.Method || '').toLowerCase().includes(keyword)
    return classMatch || methodMatch
  })
})

// 加载 Spring 统计
const loadSpringStats = async () => {
  loading.value = true
  try {
    const res = await getSpringStats()
    if (res.data) {
      springStats.value = res.data
    }
    emit('loaded', springStats.value)
  } catch (error) {
    console.error('Failed to load spring stats:', error)
  } finally {
    loading.value = false
  }
}

// 刷新
const refresh = () => {
  loadSpringStats()
}

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

// 获取简单类名
const getSimpleClassName = (fullClassName) => {
  if (!fullClassName) return '-'
  const parts = fullClassName.split('.')
  return parts[parts.length - 1]
}

// 获取方法名（不含参数）
const getMethodName = (method) => {
  if (!method) return '-'
  const match = method.match(/^([^(]+)/)
  return match ? match[1] : method
}

// 获取方法参数
const getMethodParams = (method) => {
  if (!method) return ''
  const match = method.match(/\((.*)\)$/)
  return match ? `(${match[1]})` : ''
}

// 获取执行时间类型标签
const getExecuteTimeType = (time) => {
  if (time < 10) return 'success'
  if (time < 100) return 'warning'
  return 'danger'
}

// 直方图标签
const histogramLabels = ['<1ms', '1-10ms', '10-100ms', '100ms-1s', '1-10s', '10-100s', '>100s', '错误']
const getHistogramLabel = (index) => {
  return histogramLabels[index] || `区间${index + 1}`
}

// 计算直方图高度
const getHistogramHeight = (histogram, value) => {
  const max = Math.max(...histogram, 1)
  const height = (value / max) * 100
  return `${Math.max(height, 5)}%`
}

// 展开行变化
const handleExpandChange = (row, expandedRows) => {
  // 可以在这里添加展开时的逻辑
}

onMounted(() => {
  loadSpringStats()

  if (props.autoRefresh) {
    refreshTimer = setInterval(() => {
      loadSpringStats()
    }, props.refreshInterval)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

defineExpose({
  refresh
})
</script>

<style scoped>
.spring-stats {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计概览卡片 */
.stats-overview {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-card.primary {
  border-left: 4px solid #409eff;
}

.stat-card.success {
  border-left: 4px solid #67c23a;
}

.stat-card.warning {
  border-left: 4px solid #e6a23c;
}

.stat-card.danger {
  border-left: 4px solid #f56c6c;
}

.stat-card.info {
  border-left: 4px solid #909399;
}

.stat-card.purple {
  border-left: 4px solid #9c27b0;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  margin-right: 12px;
  font-size: 20px;
}

.stat-card.primary .stat-icon {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.stat-card.success .stat-icon {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.stat-card.warning .stat-icon {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.stat-card.danger .stat-icon {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.stat-card.info .stat-icon {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.stat-card.purple .stat-icon {
  background: rgba(156, 39, 176, 0.1);
  color: #9c27b0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-value .unit {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
  margin-left: 4px;
}

.stat-value.text-danger {
  color: #f56c6c;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 表格区域 */
.table-section {
  margin-top: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.count-badge {
  margin-left: 4px;
}

/* 表格样式 */
:deep(.el-table__expanded-cell) {
  padding: 20px !important;
  background: #f5f7fa;
}

.expand-detail {
  padding: 0;
}

.detail-row {
  margin-bottom: 20px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #ffffff;
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 13px;
}

.detail-label {
  color: #606266;
}

.detail-value {
  font-weight: 600;
  color: #303133;
}

/* 方法名样式 */
.method-name {
  font-weight: 600;
  color: #409eff;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
}

.method-params {
  color: #909399;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 11px;
  margin-left: 4px;
}

/* 计数徽章 */
.count-badge {
  display: inline-block;
  min-width: 28px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  background: #f4f4f5;
  color: #909399;
}

.count-badge.jdbc {
  background: rgba(156, 39, 176, 0.1);
  color: #9c27b0;
}

/* 直方图 */
.histogram-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 120px;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
}

.histogram-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.histogram-bar {
  width: 100%;
  min-height: 4px;
  background: linear-gradient(to top, #409eff, #79bbff);
  border-radius: 4px 4px 0 0;
  transition: all 0.3s;
  cursor: pointer;
}

.histogram-bar:hover {
  background: linear-gradient(to top, #66b1ff, #a0cfff);
}

.histogram-label {
  font-size: 10px;
  color: #909399;
  text-align: center;
  white-space: nowrap;
}

.histogram-value {
  font-size: 11px;
  font-weight: 600;
  color: #606266;
}

/* 文本样式 */
.text-muted {
  color: #909399;
}

.text-warning {
  color: #e6a23c;
}

/* 空状态 */
.empty-tip {
  padding: 40px;
}

/* 响应式 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .stat-card {
    margin-bottom: 12px;
    padding: 12px;
  }

  .stat-icon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .stat-value {
    font-size: 18px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-header .el-input {
    width: 100% !important;
  }

  .histogram-container {
    gap: 4px;
    padding: 8px;
  }

  .histogram-label {
    font-size: 8px;
    transform: rotate(-45deg);
    transform-origin: center;
  }
}
</style>
