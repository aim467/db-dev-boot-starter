<template>
  <div class="session-stats">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><User /></el-icon>
            会话统计
          </span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索 Session ID"
              prefix-icon="Search"
              clearable
              size="small"
              style="width: 240px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" size="small" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button type="primary" size="small" @click="refresh" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计概览 -->
      <el-row :gutter="20" class="stats-overview">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ statsSummary.totalSessions }}</div>
            <div class="stat-label">总会话数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ statsSummary.activeSessions }}</div>
            <div class="stat-label">活跃会话</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ formatNumber(statsSummary.totalRequests) }}</div>
            <div class="stat-label">总请求数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ statsSummary.totalJdbcExecute }}</div>
            <div class="stat-label">JDBC执行数</div>
          </div>
        </el-col>
      </el-row>

      <!-- 会话表格 -->
      <el-table
        :data="filteredSessionStats"
        stripe
        style="width: 100%"
        max-height="500"
        v-loading="loading"
        row-key="SESSIONID"
        :default-sort="{ prop: 'RequestCount', order: 'descending' }"
        @sort-change="handleSortChange"
        @expand-change="handleExpandChange"
      >
        <el-table-column type="expand" width="50">
          <template #default="{ row }">
            <div class="session-detail">
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item label="会话ID">{{ row.SESSIONID }}</el-descriptions-item>
                <el-descriptions-item label="用户身份">{{ row.Principal || '匿名' }}</el-descriptions-item>
                <el-descriptions-item label="远程地址">{{ row.RemoteAddress }}</el-descriptions-item>
                <el-descriptions-item label="UserAgent" :span="3">
                  <span class="user-agent">{{ row.UserAgent }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatDateTime(row.CreateTime) }}</el-descriptions-item>
                <el-descriptions-item label="最后访问">{{ formatDateTime(row.LastAccessTime) }}</el-descriptions-item>
                <el-descriptions-item label="请求间隔分布">
                  <el-tooltip
                    :content="getRequestIntervalTooltip(row.RequestInterval)"
                    placement="top"
                  >
                    <div class="mini-histogram">
                      <div
                        v-for="(count, index) in (row.RequestInterval || [0,0,0,0,0,0,0,0,0])"
                        :key="index"
                        :class="['mini-histogram-item', `mini-histogram-${index}`]"
                        :style="{ height: getMiniHistogramHeight(count, row.RequestCount) }"
                      />
                    </div>
                  </el-tooltip>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="SESSIONID" label="会话ID" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.SESSIONID" placement="top" :show-after="500">
              <span class="session-id">{{ row.SESSIONID }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="Principal" label="用户" width="120">
          <template #default="{ row }">
            {{ row.Principal || '匿名' }}
          </template>
        </el-table-column>
        <el-table-column prop="RemoteAddress" label="远程地址" width="140" />
        <el-table-column prop="RequestCount" label="请求数" width="100" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.RequestCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="RunningCount" label="运行中" width="90" sortable>
          <template #default="{ row }">
            <span :class="{ 'running-text': row.RunningCount > 0 }">
              {{ row.RunningCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="ConcurrentMax" label="最大并发" width="110" sortable />
        <el-table-column prop="RequestTimeMillisTotal" label="总耗时(ms)" width="130" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.RequestTimeMillisTotal) }}
          </template>
        </el-table-column>
        <el-table-column prop="JdbcExecuteCount" label="JDBC执行" width="110" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.JdbcExecuteCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="JdbcExecuteTimeMillis" label="JDBC耗时(ms)" width="140" sortable>
          <template #default="{ row }">
            <span :class="getTimeClass(row.JdbcExecuteTimeMillis)">
              {{ formatNumber(row.JdbcExecuteTimeMillis) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="JdbcFetchRowCount" label="查询行数" width="110" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.JdbcFetchRowCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="JdbcUpdateCount" label="更新行数" width="110" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.JdbcUpdateCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="JdbcCommitCount" label="提交数" width="100" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.JdbcCommitCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="JdbcRollbackCount" label="回滚数" width="100" sortable>
          <template #default="{ row }">
            <span :class="{ 'error-text': row.JdbcRollbackCount > 0 }">
              {{ formatNumber(row.JdbcRollbackCount) }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredSessionStats.length === 0 && !loading" class="empty-tip">
        <el-empty description="暂无会话统计数据">
          <el-button type="primary" @click="refresh">刷新数据</el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {getSessionStats} from '@/api/druid'

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

const emit = defineEmits(['loaded'])

const loading = ref(false)
const sessionStats = ref([])
const searchKeyword = ref('')
const sortProp = ref('RequestCount')
const sortOrder = ref('descending')

let refreshTimer = null

// 过滤后的数据
const filteredSessionStats = computed(() => {
  let data = [...sessionStats.value]

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    data = data.filter(item =>
      item.SESSIONID && item.SESSIONID.toLowerCase().includes(keyword)
    )
  }

  // 排序
  if (sortProp.value) {
    data.sort((a, b) => {
      const aVal = a[sortProp.value] || 0
      const bVal = b[sortProp.value] || 0
      return sortOrder.value === 'ascending' ? aVal - bVal : bVal - aVal
    })
  }

  return data
})

// 统计概览
const statsSummary = computed(() => {
  const total = sessionStats.value.length
  const active = sessionStats.value.filter(item => item.RunningCount > 0).length
  const totalRequests = sessionStats.value.reduce((sum, item) => sum + (item.RequestCount || 0), 0)
  const totalJdbcExecute = sessionStats.value.reduce((sum, item) => sum + (item.JdbcExecuteCount || 0), 0)

  return {
    totalSessions: total,
    activeSessions: active,
    totalRequests,
    totalJdbcExecute
  }
})

// 加载会话统计
const loadSessionStats = async () => {
  loading.value = true
  try {
    const res = await getSessionStats()
    sessionStats.value = res.data || []
    emit('loaded', sessionStats.value)
  } catch (error) {
    console.error('Failed to load session stats:', error)
  } finally {
    loading.value = false
  }
}

// 刷新
const refresh = () => {
  loadSessionStats()
}

// 搜索
const handleSearch = () => {
  // 搜索通过 computed 的 filteredSessionStats 自动处理
}

// 排序
const handleSortChange = ({ prop, order }) => {
  sortProp.value = prop
  sortOrder.value = order
}

// 处理展开行变化
const expandedRows = ref([])
const handleExpandChange = (row, expandedRowsList) => {
  expandedRows.value = expandedRowsList
}

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN')
}

// 获取请求间隔分布提示
const getRequestIntervalTooltip = (intervals) => {
  if (!intervals || intervals.length < 9) return '无数据'
  const labels = ['<1ms', '1-10ms', '10-100ms', '100-1000ms', '1-10s', '10-100s', '100-1000s', '>1000s', '未知']
  return intervals.map((count, index) => `${labels[index]}: ${count}`).join(' | ')
}

// 获取时间样式类
const getTimeClass = (time) => {
  if (time > 3000) return 'slow-query-high'
  if (time > 1000) return 'slow-query'
  return ''
}

// 获取迷你直方图高度
const getMiniHistogramHeight = (count, total) => {
  if (!total || total === 0) return '4px'
  const percentage = (count / total) * 100
  return `${Math.max(percentage, 4)}%`
}

onMounted(() => {
  loadSessionStats()

  if (props.autoRefresh) {
    refreshTimer = setInterval(() => {
      loadSessionStats()
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
.session-stats {
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

.session-id {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 12px;
  color: #606266;
}

.user-agent {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}

.running-text {
  color: #67c23a;
  font-weight: 700;
}

.slow-query {
  color: #e6a23c;
  font-weight: 600;
}

.slow-query-high {
  color: #f56c6c;
  font-weight: 700;
}

.error-text {
  color: #f56c6c;
  font-weight: 700;
}

.session-detail {
  padding: 12px 24px;
  background: #f5f7fa;
  border-radius: 4px;
  margin: 8px 0;
}

.mini-histogram {
  display: flex;
  align-items: flex-end;
  gap: 1px;
  height: 20px;
  width: 60px;
}

.mini-histogram-item {
  flex: 1;
  min-height: 3px;
  border-radius: 1px;
  transition: all 0.3s;
}

.mini-histogram-0 { background: #67c23a; }
.mini-histogram-1 { background: #67c23a; }
.mini-histogram-2 { background: #409eff; }
.mini-histogram-3 { background: #409eff; }
.mini-histogram-4 { background: #e6a23c; }
.mini-histogram-5 { background: #e6a23c; }
.mini-histogram-6 { background: #f56c6c; }
.mini-histogram-7 { background: #f56c6c; }
.mini-histogram-8 { background: #909399; }

.mini-histogram-item:hover {
  opacity: 0.8;
  transform: scaleY(1.1);
}

.empty-tip {
  padding: 40px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
