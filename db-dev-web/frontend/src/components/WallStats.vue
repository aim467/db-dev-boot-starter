<template>
  <div class="wall-stats">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Lock /></el-icon>
            Wall 防火墙统计
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
              <el-icon><Search /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(wallStats.checkCount) }}</div>
              <div class="stat-label">检查次数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card warning">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value" :class="{ 'text-danger': wallStats.violationCount > 0 }">
                {{ formatNumber(wallStats.violationCount) }}
              </div>
              <div class="stat-label">违规次数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card danger">
            <div class="stat-icon">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value" :class="{ 'text-danger': wallStats.blackListHitCount > 0 }">
                {{ formatNumber(wallStats.blackListHitCount) }}
              </div>
              <div class="stat-label">黑名单命中</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card success">
            <div class="stat-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(wallStats.whiteListHitCount) }}</div>
              <div class="stat-label">白名单命中</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card info">
            <div class="stat-icon">
              <el-icon><DocumentChecked /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(wallStats.whiteListSize) }}</div>
              <div class="stat-label">白名单规则</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6" :lg="4">
          <div class="stat-card" :class="wallStats.syntaxErrorCount > 0 ? 'danger' : 'success'">
            <div class="stat-icon">
              <el-icon><DocumentDelete /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value" :class="{ 'text-danger': wallStats.syntaxErrorCount > 0 }">
                {{ formatNumber(wallStats.syntaxErrorCount) }}
              </div>
              <div class="stat-label">语法错误</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 详细统计标签页 -->
      <el-tabs v-model="activeTab" type="border-card" class="detail-tabs">
        <!-- 表操作统计 -->
        <el-tab-pane name="tables">
          <template #label>
            <span class="tab-label">
              <el-icon><Grid /></el-icon>
              表操作统计
              <el-tag v-if="tableStats.length > 0" size="small" type="info" class="tab-badge">
                {{ tableStats.length }}
              </el-tag>
            </span>
          </template>

          <el-table
            :data="tableStats"
            stripe
            style="width: 100%"
            max-height="400"
            v-loading="loading"
            :default-sort="{ prop: 'totalCount', order: 'descending' }"
          >
            <el-table-column prop="name" label="表名" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag size="small" effect="plain" type="info">
                  {{ row.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="selectCount" label="查询" width="100" sortable align="center">
              <template #default="{ row }">
                <span class="count-badge select">{{ formatNumber(row.selectCount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="insertCount" label="插入" width="100" sortable align="center">
              <template #default="{ row }">
                <span class="count-badge insert">{{ formatNumber(row.insertCount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="updateCount" label="更新" width="100" sortable align="center">
              <template #default="{ row }">
                <span class="count-badge update">{{ formatNumber(row.updateCount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="deleteCount" label="删除" width="100" sortable align="center">
              <template #default="{ row }">
                <span class="count-badge delete" :class="{ 'has-value': row.deleteCount > 0 }">
                  {{ formatNumber(row.deleteCount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="totalCount" label="总计" width="100" sortable align="center">
              <template #default="{ row }">
                <strong>{{ formatNumber(row.totalCount) }}</strong>
              </template>
            </el-table-column>
            <el-table-column label="操作分布" min-width="200">
              <template #default="{ row }">
                <div class="operation-bar">
                  <div
                    v-if="row.selectCount > 0"
                    class="bar-segment select"
                    :style="{ width: getOperationPercent(row, 'selectCount') }"
                    :title="`查询: ${row.selectCount}`"
                  />
                  <div
                    v-if="row.insertCount > 0"
                    class="bar-segment insert"
                    :style="{ width: getOperationPercent(row, 'insertCount') }"
                    :title="`插入: ${row.insertCount}`"
                  />
                  <div
                    v-if="row.updateCount > 0"
                    class="bar-segment update"
                    :style="{ width: getOperationPercent(row, 'updateCount') }"
                    :title="`更新: ${row.updateCount}`"
                  />
                  <div
                    v-if="row.deleteCount > 0"
                    class="bar-segment delete"
                    :style="{ width: getOperationPercent(row, 'deleteCount') }"
                    :title="`删除: ${row.deleteCount}`"
                  />
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="tableStats.length === 0 && !loading" class="empty-tip">
            <el-empty description="暂无表操作数据" />
          </div>
        </el-tab-pane>

        <!-- 白名单规则 -->
        <el-tab-pane name="whitelist">
          <template #label>
            <span class="tab-label">
              <el-icon><CircleCheck /></el-icon>
              白名单规则
              <el-tag v-if="wallStats.whiteList?.length > 0" size="small" type="success" class="tab-badge">
                {{ wallStats.whiteList.length }}
              </el-tag>
            </span>
          </template>

          <el-table
            :data="wallStats.whiteList || []"
            stripe
            style="width: 100%"
            max-height="400"
            v-loading="loading"
          >
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="sql" label="SQL 语句" min-width="400" show-overflow-tooltip>
              <template #default="{ row }">
                <code class="sql-code">{{ row.sql }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="executeCount" label="执行次数" width="120" sortable align="center">
              <template #default="{ row }">
                <el-tag size="small" type="success">{{ formatNumber(row.executeCount) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="!(wallStats.whiteList?.length) && !loading" class="empty-tip">
            <el-empty description="暂无白名单规则" />
          </div>
        </el-tab-pane>

        <!-- 黑名单规则 -->
        <el-tab-pane name="blacklist">
          <template #label>
            <span class="tab-label">
              <el-icon><CircleClose /></el-icon>
              黑名单规则
              <el-tag v-if="wallStats.blackList?.length > 0" size="small" type="danger" class="tab-badge">
                {{ wallStats.blackList.length }}
              </el-tag>
            </span>
          </template>

          <el-alert
            v-if="wallStats.blackList?.length > 0"
            title="以下 SQL 模式被禁止执行"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          />

          <el-table
            :data="wallStats.blackList || []"
            stripe
            style="width: 100%"
            max-height="400"
            v-loading="loading"
          >
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="sql" label="SQL 模式" min-width="500" show-overflow-tooltip>
              <template #default="{ row }">
                <code class="sql-code danger">{{ row.sql || row }}</code>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="!(wallStats.blackList?.length) && !loading" class="empty-tip">
            <el-empty description="暂无黑名单规则">
              <template #description>
                <div style="text-align: center;">
                  <p>暂无黑名单规则</p>
                  <p style="color: #909399; font-size: 12px; margin-top: 8px;">
                    黑名单用于禁止特定的 SQL 执行
                  </p>
                </div>
              </template>
            </el-empty>
          </div>
        </el-tab-pane>

        <!-- 函数统计 -->
        <el-tab-pane name="functions">
          <template #label>
            <span class="tab-label">
              <el-icon><FunctionIcon /></el-icon>
              函数统计
              <el-tag v-if="functionStats.length > 0" size="small" type="info" class="tab-badge">
                {{ functionStats.length }}
              </el-tag>
            </span>
          </template>

          <el-table
            :data="functionStats"
            stripe
            style="width: 100%"
            max-height="400"
            v-loading="loading"
          >
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="name" label="函数名" min-width="200">
              <template #default="{ row }">
                <el-tag size="small" type="warning" effect="plain">
                  <el-icon><Connection /></el-icon>
                  {{ row.name }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="invokeCount" label="调用次数" width="120" sortable align="center">
              <template #default="{ row }">
                <strong>{{ formatNumber(row.invokeCount) }}</strong>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="functionStats.length === 0 && !loading" class="empty-tip">
            <el-empty description="暂无函数统计数据" />
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 违规信息提示 -->
      <el-alert
        v-if="wallStats.violationCount > 0"
        :title="`检测到 ${wallStats.violationCount} 次违规操作`"
        type="error"
        :closable="false"
        show-icon
        style="margin-top: 20px;"
      >
        <template #default>
          <div style="margin-top: 8px;">
            <p v-if="wallStats.violationEffectRowCount > 0">
              违规影响行数: {{ formatNumber(wallStats.violationEffectRowCount) }}
            </p>
            <p>请检查应用日志了解详细信息，或审查黑白名单配置。</p>
          </div>
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { computed, h, onMounted, onUnmounted, ref } from 'vue'
import { getWallStats } from '@/api/druid'
import {
  Lock,
  Refresh,
  Search,
  Warning,
  CircleClose,
  CircleCheck,
  DocumentChecked,
  DocumentDelete,
  Grid,
  Document,
  Connection
} from '@element-plus/icons-vue'

// 自定义函数图标组件
const FunctionIcon = {
  render() {
    return h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      width: '1em',
      height: '1em'
    }, [
      h('path', { d: 'M6 4h4v16H6z' }),
      h('path', { d: 'M14 4h4v4h-4z' }),
      h('path', { d: 'M14 10h4v4h-4z' }),
      h('path', { d: 'M14 16h4v4h-4z' })
    ])
  }
}

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
const wallStats = ref({
  checkCount: 0,
  hardCheckCount: 0,
  violationCount: 0,
  violationEffectRowCount: 0,
  blackListHitCount: 0,
  blackListSize: 0,
  whiteListHitCount: 0,
  whiteListSize: 0,
  syntaxErrorCount: 0,
  tables: [],
  functions: [],
  blackList: [],
  whiteList: []
})
const activeTab = ref('tables')

let refreshTimer = null

// 计算表统计数据（添加总计列）
const tableStats = computed(() => {
  const tables = wallStats.value.tables || []
  return tables.map(table => ({
    ...table,
    selectCount: table.selectCount || 0,
    insertCount: table.insertCount || 0,
    updateCount: table.updateCount || 0,
    deleteCount: table.deleteCount || 0,
    totalCount: (table.selectCount || 0) + (table.insertCount || 0) + (table.updateCount || 0) + (table.deleteCount || 0)
  })).sort((a, b) => b.totalCount - a.totalCount)
})

// 计算函数统计数据
const functionStats = computed(() => {
  const functions = wallStats.value.functions || []
  return functions.map(fn => ({
    name: fn.name || fn,
    invokeCount: fn.invokeCount || 0
  })).sort((a, b) => b.invokeCount - a.invokeCount)
})

// 加载 Wall 统计
const loadWallStats = async () => {
  loading.value = true
  try {
    const res = await getWallStats()
    if (res.data) {
      wallStats.value = {
        ...wallStats.value,
        ...res.data
      }
    }
    emit('loaded', wallStats.value)
  } catch (error) {
    console.error('Failed to load wall stats:', error)
  } finally {
    loading.value = false
  }
}

// 刷新
const refresh = () => {
  loadWallStats()
}

// 格式化数字
const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

// 获取操作百分比（用于进度条）
const getOperationPercent = (row, type) => {
  if (row.totalCount === 0) return '0%'
  const count = row[type] || 0
  return `${Math.max((count / row.totalCount) * 100, 5)}%`
}

onMounted(() => {
  loadWallStats()

  if (props.autoRefresh) {
    refreshTimer = setInterval(() => {
      loadWallStats()
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
.wall-stats {
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

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-value.text-danger {
  color: #f56c6c;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 详细标签页 */
.detail-tabs {
  margin-top: 8px;
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-badge {
  margin-left: 4px;
}

/* 表格样式 */
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

.count-badge.select {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.count-badge.insert {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.count-badge.update {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.count-badge.delete {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.count-badge.delete.has-value {
  background: #f56c6c;
  color: #fff;
}

/* 操作分布条 */
.operation-bar {
  display: flex;
  height: 20px;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f7fa;
}

.bar-segment {
  min-width: 4px;
  transition: all 0.3s;
  cursor: pointer;
}

.bar-segment:hover {
  opacity: 0.8;
}

.bar-segment.select {
  background: #409eff;
}

.bar-segment.insert {
  background: #67c23a;
}

.bar-segment.update {
  background: #e6a23c;
}

.bar-segment.delete {
  background: #f56c6c;
}

/* SQL 代码样式 */
.sql-code {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  word-break: break-all;
}

.sql-code.danger {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
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
  }

  .stat-icon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .stat-value {
    font-size: 18px;
  }
}
</style>
