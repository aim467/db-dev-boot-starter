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

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

