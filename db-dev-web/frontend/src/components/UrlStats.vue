<template>
  <div class="url-stats">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Link /></el-icon>
            URL 访问统计
          </span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="refresh" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20" class="stats-overview">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ statsSummary.totalUrl }}</div>
            <div class="stat-label">URL 数量</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ formatNumber(statsSummary.totalRequest) }}</div>
            <div class="stat-label">总请求次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ formatNumber(statsSummary.totalError) }}</div>
            <div class="stat-label">总错误次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ statsSummary.avgTime }}ms</div>
            <div class="stat-label">平均请求耗时</div>
          </div>
        </el-col>
      </el-row>

      <el-table
        :data="filteredUrlStats"
        stripe
        style="width: 100%"
        max-height="500"
        v-loading="loading"
        row-key="uri"
        :default-sort="{ prop: 'requestCount', order: 'descending' }"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="uri" label="URL" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.uri" placement="top" :show-after="500">
              <span class="url-text">{{ row.uri }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="requestCount" label="请求次数" width="120" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.requestCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="requestTimeMillis" label="总耗时(ms)" width="140" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.requestTimeMillis) }}
          </template>
        </el-table-column>
        <el-table-column prop="runningCount" label="当前请求数" width="120" sortable />
        <el-table-column prop="concurrentMax" label="最大并发" width="120" sortable />
        <el-table-column prop="jdbcExecuteCount" label="SQL 执行次数" width="140" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcExecuteCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcExecuteErrorCount" label="SQL 错误次数" width="140" sortable>
          <template #default="{ row }">
            <span :class="{ 'error-text': row.jdbcExecuteErrorCount > 0 }">
              {{ formatNumber(row.jdbcExecuteErrorCount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="jdbcExecuteTimeMillis" label="SQL 总耗时(ms)" width="160" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcExecuteTimeMillis) }}
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="错误次数" width="120" sortable>
          <template #default="{ row }">
            <span :class="{ 'error-text': row.errorCount > 0 }">
              {{ formatNumber(row.errorCount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lastAccessTime" label="最后访问时间" width="200" sortable>
          <template #default="{ row }">
            {{ formatDate(row.lastAccessTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="requestTimeMillisMax" label="最大请求耗时(ms)" width="160" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.requestTimeMillisMax) }}
          </template>
        </el-table-column>
        <el-table-column prop="requestTimeMillisMaxOccurTime" label="最大耗时发生时间" width="200" sortable>
          <template #default="{ row }">
            {{ formatDate(row.requestTimeMillisMaxOccurTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcExecutePeak" label="SQL执行峰值" width="140" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcExecutePeak) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcCommitCount" label="SQL提交次数" width="140" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcCommitCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcRollbackCount" label="SQL回滚次数" width="140" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcRollbackCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcFetchRowCount" label="SQL获取行数" width="160" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcFetchRowCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcFetchRowPeak" label="SQL获取行数峰值" width="180" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcFetchRowPeak) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcUpdateCount" label="SQL更新次数" width="140" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcUpdateCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcUpdatePeak" label="SQL更新次数峰值" width="180" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcUpdatePeak) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcPoolConnectionOpenCount" label="连接池打开次数" width="180" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcPoolConnectionOpenCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcPoolConnectionCloseCount" label="连接池关闭次数" width="180" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcPoolConnectionCloseCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcResultSetOpenCount" label="结果集打开次数" width="180" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcResultSetOpenCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="jdbcResultSetCloseCount" label="结果集关闭次数" width="180" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.jdbcResultSetCloseCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="histogram" label="请求耗时分布" width="120">
          <template #default="{ row }">
            <el-tooltip
              :content="`<1ms: ${row.histogram?.[0] || 0} | 1-10ms: ${row.histogram?.[1] || 0} | 10-100ms: ${row.histogram?.[2] || 0} | 100ms-1s: ${row.histogram?.[3] || 0} | 1-10s: ${row.histogram?.[4] || 0} | 10-100s: ${row.histogram?.[5] || 0} | >100s: ${row.histogram?.[6] || 0} | 错误: ${row.histogram?.[7] || 0}`"
              placement="left"
              raw-content
            >
              <div class="histogram-bar">
                <div
                  v-for="(count, index) in (row.histogram || [0, 0, 0, 0, 0, 0, 0, 0])"
                  :key="index"
                  :class="['histogram-item', `histogram-${index}`]"
                  :style="{ height: getHistogramHeight(count, row.requestCount) }"
                />
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredUrlStats.length === 0 && !loading" class="empty-tip">
        <el-empty description="暂无 URL 统计数据">
          <el-button type="primary" @click="refresh">刷新数据</el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {getUrlStats} from '@/api/druid'

const props = defineProps({
  autoRefresh: {
    type: Boolean,
    default: false
  },
  refreshInterval: {
    type: Number,
    default: 10000
  }
})

const loading = ref(false)
const urlStats = ref([])
const sortProp = ref('requestCount')
const sortOrder = ref('descending')

let refreshTimer = null

const filteredUrlStats = computed(() => {
  const data = [...urlStats.value]
  if (sortProp.value) {
    data.sort((a, b) => {
      const aVal = a[sortProp.value] || 0
      const bVal = b[sortProp.value] || 0
      return sortOrder.value === 'ascending' ? aVal - bVal : bVal - aVal
    })
  }
  return data
})

const statsSummary = computed(() => {
  const totalUrl = urlStats.value.length
  const totalRequest = urlStats.value.reduce((sum, item) => sum + (item.requestCount || 0), 0)
  const totalError = urlStats.value.reduce((sum, item) => sum + (item.jdbcExecuteErrorCount || 0), 0)
  const totalTime = urlStats.value.reduce((sum, item) => sum + (item.requestTimeMillis || 0), 0)
  const avgTime = totalRequest > 0 ? Math.round(totalTime / totalRequest) : 0
  return {
    totalUrl,
    totalRequest,
    totalError,
    avgTime
  }
})

const loadUrlStats = async () => {
  loading.value = true
  try {
    const res = await getUrlStats()
    urlStats.value = res.data || []
  } catch (error) {
    console.error('Failed to load URL stats:', error)
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  loadUrlStats()
}

const handleSortChange = ({prop, order}) => {
  sortProp.value = prop
  sortOrder.value = order
}

const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  })
}

const getHistogramHeight = (count, total) => {
  if (!total || total === 0) return '0%'
  const percentage = (count / total) * 100
  return Math.max(percentage, 5) + '%'
}

onMounted(() => {
  loadUrlStats()
  if (props.autoRefresh) {
    refreshTimer = setInterval(() => {
      loadUrlStats()
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
.url-stats {
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

.stats-overview {
  margin-bottom: 20px;
}

.stat-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f2f5 100%);
  padding: 20px;
  border-radius: 12px;
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.url-text {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 12px;
  color: #606266;
}

.error-text {
  color: #f56c6c;
  font-weight: 700;
}

.empty-tip {
  padding: 40px;
}

.histogram-bar {
  display: flex;
  align-items: flex-end;
  height: 40px;
  gap: 2px;
  padding: 4px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: pointer;
}

.histogram-item {
  flex: 1;
  min-width: 4px;
  border-radius: 2px 2px 0 0;
  transition: all 0.3s;
}

.histogram-0 { background: #67c23a; }
.histogram-1 { background: #67c23a; }
.histogram-2 { background: #409eff; }
.histogram-3 { background: #409eff; }
.histogram-4 { background: #e6a23c; }
.histogram-5 { background: #e6a23c; }
.histogram-6 { background: #f56c6c; }
.histogram-7 { background: #909399; }

.histogram-item:hover {
  opacity: 0.8;
  transform: scaleY(1.1);
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

