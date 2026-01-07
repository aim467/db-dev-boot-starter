<template>
  <div class="sql-stats">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Document /></el-icon>
            SQL 统计
          </span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索 SQL"
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
            <div class="stat-value">{{ statsSummary.totalSql }}</div>
            <div class="stat-label">SQL 语句数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ formatNumber(statsSummary.totalExecute) }}</div>
            <div class="stat-label">总执行次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ formatNumber(statsSummary.totalError) }}</div>
            <div class="stat-label">错误次数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ statsSummary.avgExecuteTime }}ms</div>
            <div class="stat-label">平均执行时间</div>
          </div>
        </el-col>
      </el-row>

      <!-- SQL 表格 -->
      <el-table
        :data="filteredSqlStats"
        stripe
        style="width: 100%"
        max-height="500"
        v-loading="loading"
        row-key="id"
        :default-sort="{ prop: 'executeCount', order: 'descending' }"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="sql" label="SQL" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip :content="row.sql" placement="top" :show-after="500">
              <span class="sql-text">{{ row.sql }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="executeCount" label="执行次数" width="120" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.executeCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="fetchRowCount" label="返回行数" width="120" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.fetchRowCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalTime" label="总耗时(ms)" width="130" sortable>
          <template #default="{ row }">
            {{ formatNumber(row.totalTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="avgTime" label="平均耗时(ms)" width="140" sortable>
          <template #default="{ row }">
            <span :class="getTimeClass(row.avgTime)">
              {{ row.avgTime }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="maxTime" label="最大耗时(ms)" width="140" sortable>
          <template #default="{ row }">
            <span :class="getTimeClass(row.maxTime)">
              {{ formatNumber(row.maxTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="错误次数" width="120" sortable>
          <template #default="{ row }">
            <span :class="{ 'error-text': row.errorCount > 0 }">
              {{ row.errorCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="runningCount" label="执行中" width="80" />
        <el-table-column prop="histogram" label="耗时分布" width="150">
          <template #default="{ row }">
            <el-tooltip
              :content="`0-10ms: ${row.histogram?.[0] || 0} | 10-100ms: ${row.histogram?.[1] || 0} | 100-1000ms: ${row.histogram?.[2] || 0} | >1000ms: ${row.histogram?.[3] || 0}`"
              placement="left"
            >
              <div class="histogram-bar">
                <div
                  v-for="(count, index) in (row.histogram || [0, 0, 0, 0])"
                  :key="index"
                  :class="['histogram-item', `histogram-${index}`]"
                  :style="{ height: getHistogramHeight(count, row.executeCount) }"
                />
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredSqlStats.length === 0 && !loading" class="empty-tip">
        <el-empty description="暂无 SQL 统计数据">
          <el-button type="primary" @click="refresh">刷新数据</el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getSqlStats } from '@/api/druid'

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
const sqlStats = ref([])
const searchKeyword = ref('')
const sortProp = ref('executeCount')
const sortOrder = ref('descending')

let refreshTimer = null

// 过滤后的数据
const filteredSqlStats = computed(() => {
  let data = [...sqlStats.value]
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    data = data.filter(item => 
      item.sql && item.sql.toLowerCase().includes(keyword)
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
  const total = sqlStats.value.length
  const totalExecute = sqlStats.value.reduce((sum, item) => sum + (item.executeCount || 0), 0)
  const totalError = sqlStats.value.reduce((sum, item) => sum + (item.errorCount || 0), 0)
  const totalTime = sqlStats.value.reduce((sum, item) => sum + (item.totalTime || 0), 0)
  const avgExecuteTime = totalExecute > 0 ? Math.round(totalTime / totalExecute) : 0
  
  return {
    totalSql: total,
    totalExecute,
    totalError,
    avgExecuteTime
  }
})

// 加载 SQL 统计
const loadSqlStats = async () => {
  loading.value = true
  try {
    const res = await getSqlStats()
    sqlStats.value = res.data || []
    emit('loaded', sqlStats.value)
  } catch (error) {
    console.error('Failed to load SQL stats:', error)
  } finally {
    loading.value = false
  }
}

// 刷新
const refresh = () => {
  loadSqlStats()
}

// 搜索
const handleSearch = () => {
  // 搜索通过 computed 的 filteredSqlStats 自动处理
}

// 排序
const handleSortChange = ({ prop, order }) => {
  sortProp.value = prop
  sortOrder.value = order
}

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

// 获取时间样式类
const getTimeClass = (time) => {
  if (time > 3000) return 'slow-query-high'
  if (time > 1000) return 'slow-query'
  return ''
}

// 获取直方图高度
const getHistogramHeight = (count, total) => {
  if (!total || total === 0) return '4px'
  const percentage = (count / total) * 100
  return `${Math.max(percentage, 4)}%`
}

onMounted(() => {
  loadSqlStats()
  
  if (props.autoRefresh) {
    refreshTimer = setInterval(() => {
      loadSqlStats()
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
.sql-stats {
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

.sql-text {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 12px;
  color: #606266;
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

.histogram-bar {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 24px;
}

.histogram-item {
  flex: 1;
  min-height: 4px;
  border-radius: 2px;
  transition: all 0.3s;
}

.histogram-0 {
  background: #67c23a;
}

.histogram-1 {
  background: #409eff;
}

.histogram-2 {
  background: #e6a23c;
}

.histogram-3 {
  background: #f56c6c;
}

.histogram-item:hover {
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
