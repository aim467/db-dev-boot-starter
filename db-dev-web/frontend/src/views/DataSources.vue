<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon><Connection /></el-icon>
            <span style="font-weight: 600;">数据源列表</span>
            <el-tag v-if="datasourceStore.dataSources.length > 0" type="info" size="small">
              {{ datasourceStore.dataSources.length }}
            </el-tag>
          </div>
          <div style="display: flex; gap: 8px;">
            <el-button type="primary" size="small" @click="loadData" :loading="datasourceStore.loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="success" size="small" @click="testAllConnections">
              <el-icon><Link /></el-icon>
              测试连接
            </el-button>
          </div>
        </div>
      </template>
      <div v-loading="datasourceStore.loading">
        <el-empty v-if="!datasourceStore.loading && datasourceStore.dataSources.length === 0" description="未找到数据源">
          <el-button type="primary" size="small">添加数据源</el-button>
        </el-empty>
        <el-table v-else :data="datasourceStore.dataSources" stripe style="width: 100%;">
          <el-table-column prop="name" label="名称" width="200" fixed>
            <template #default="scope">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-icon v-if="scope.row.primary" color="#f56c6c"><Star /></el-icon>
                <strong>{{ scope.row.name }}</strong>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="databaseType" label="数据库类型" width="150">
            <template #default="scope">
              <el-tag :type="getDatabaseTypeColor(scope.row.databaseType)" size="small">
                {{ scope.row.databaseType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="连接池类型" width="180" />
          <el-table-column prop="url" label="URL" min-width="300" show-overflow-tooltip>
            <template #default="scope">
              <el-text type="info" size="small" style="font-family: monospace;">{{ scope.row.url }}</el-text>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column label="状态" width="140">
            <template #default="scope">
              <el-tag :type="scope.row.status || 'success'" size="small">
                <el-icon><CircleCheck /></el-icon>
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button-group>
                <el-button type="primary" size="small" @click="showPoolMonitor(scope.row)">
                  <el-icon><DataLine /></el-icon>
                  监控
                </el-button>
                <el-button type="success" size="small" @click="testConnection(scope.row)">
                  <el-icon><Link /></el-icon>
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 连接池监控抽屉 -->
    <el-drawer v-model="poolMonitorVisible" direction="rtl" size="550px">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon color="#409eff"><DataLine /></el-icon>
            <span style="font-weight: 600;">连接池监控</span>
            <el-tag type="info" size="small">{{ currentDataSource?.name }}</el-tag>
          </div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-switch v-model="autoRefresh" active-text="自动刷新" size="small" @change="toggleAutoRefresh" />
            <el-button type="primary" size="small" @click="loadPoolStats" :loading="poolStatsLoading">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="poolStatsLoading">
        <el-alert v-if="poolStats" :title="'连接池类型: ' + poolStats.poolType" type="info" :closable="false" show-icon style="margin-bottom: 20px;" />
        <el-alert v-if="poolStats && poolStats.activeConnections === -1" title="该连接池类型暂不支持详细监控" type="warning" :closable="false" show-icon style="margin-bottom: 20px;" />

        <!-- 统计卡片 -->
        <el-row :gutter="12" v-if="poolStats && poolStats.activeConnections !== -1">
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card stat-card-active">
              <div class="stat-value">{{ poolStats.activeConnections }}</div>
              <div class="stat-label">活跃连接</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card stat-card-idle">
              <div class="stat-value">{{ poolStats.idleConnections }}</div>
              <div class="stat-label">空闲连接</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="stat-card stat-card-total">
              <div class="stat-value">{{ poolStats.totalConnections }}</div>
              <div class="stat-label">总连接数</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 使用率进度条 -->
        <el-card shadow="hover" style="margin-top: 16px;" v-if="poolStats && poolStats.activeConnections !== -1">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon><TrendCharts /></el-icon>
              <span style="font-weight: 600;">连接使用率</span>
            </div>
          </template>
          <div style="padding: 10px 0;">
            <el-progress :percentage="poolStats.usagePercent" :color="getUsageColor(poolStats.usagePercent)" :stroke-width="20" :format="(p) => p.toFixed(1) + '%'" />
            <div style="display: flex; justify-content: space-between; margin-top: 12px; color: #909399; font-size: 12px;">
              <span>活跃: {{ poolStats.activeConnections }} / 最大: {{ poolStats.maxConnections }}</span>
              <span v-if="poolStats.waitingThreads > 0" style="color: #f56c6c;">
                <el-icon><Warning /></el-icon> {{ poolStats.waitingThreads }} 个线程等待中
              </span>
            </div>
          </div>
        </el-card>

        <!-- 配置信息 -->
        <el-card shadow="hover" style="margin-top: 16px;" v-if="poolStats && poolStats.activeConnections !== -1">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon><Setting /></el-icon>
              <span style="font-weight: 600;">连接池配置</span>
            </div>
          </template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="最大连接数">{{ poolStats.maxConnections }}</el-descriptions-item>
            <el-descriptions-item label="最小空闲">{{ poolStats.minIdle }}</el-descriptions-item>
            <el-descriptions-item label="连接超时" v-if="poolStats.connectionTimeout">{{ formatDuration(poolStats.connectionTimeout) }}</el-descriptions-item>
            <el-descriptions-item label="空闲超时" v-if="poolStats.idleTimeout">{{ formatDuration(poolStats.idleTimeout) }}</el-descriptions-item>
            <el-descriptions-item label="最大生命周期" v-if="poolStats.maxLifetime">{{ formatDuration(poolStats.maxLifetime) }}</el-descriptions-item>
            <el-descriptions-item label="等待线程">
              <el-tag :type="poolStats.waitingThreads > 0 ? 'danger' : 'success'" size="small">{{ poolStats.waitingThreads }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Druid 额外统计 -->
        <el-card shadow="hover" style="margin-top: 16px;" v-if="poolStats && poolStats.createCount != null">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon><Histogram /></el-icon>
              <span style="font-weight: 600;">累计统计</span>
            </div>
          </template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="累计创建连接">{{ poolStats.createCount }}</el-descriptions-item>
            <el-descriptions-item label="累计销毁连接">{{ poolStats.destroyCount }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { useDatasourceStore } from '@/stores/datasource'
import { testConnection as testConnectionApi, getPoolStats } from '@/api/datasource'

const datasourceStore = useDatasourceStore()

const poolMonitorVisible = ref(false)
const currentDataSource = ref(null)
const poolStats = ref(null)
const poolStatsLoading = ref(false)
const autoRefresh = ref(false)
let refreshTimer = null

const getDatabaseTypeColor = (type) => {
  const typeMap = { 'MySQL': 'success', 'PostgreSQL': 'warning', 'Oracle': 'danger', 'SQL Server': 'info', 'H2': '' }
  return typeMap[type] || 'info'
}

const loadData = async () => {
  try {
    await datasourceStore.loadDataSources()
    ElMessage.success(`成功加载 ${datasourceStore.dataSources.length} 个数据源`)
  } catch (error) {
    ElMessage.error('加载数据源失败: ' + error.message)
  }
}

const testConnection = async (dataSource) => {
  try {
    await testConnectionApi(dataSource.name)
    ElMessage.success(`数据源 "${dataSource.name}" 连接成功`)
    dataSource.status = 'success'
  } catch (error) {
    ElMessage.error(`数据源 "${dataSource.name}" 连接失败`)
    dataSource.status = 'error'
  }
}

const testAllConnections = async () => {
  const loading = ElLoading.service({ lock: true, text: '正在测试所有连接...', background: 'rgba(0, 0, 0, 0.7)' })
  try {
    await Promise.all(datasourceStore.dataSources.map(ds => testConnection(ds)))
    ElMessage.success('所有连接测试完成')
  } finally {
    loading.close()
  }
}

const showPoolMonitor = async (dataSource) => {
  currentDataSource.value = dataSource
  poolMonitorVisible.value = true
  await loadPoolStats()
}

const loadPoolStats = async () => {
  if (!currentDataSource.value) return
  poolStatsLoading.value = true
  try {
    const res = await getPoolStats(currentDataSource.value.name)
    poolStats.value = res.data
  } catch (error) {
    ElMessage.error('获取连接池统计失败: ' + error.message)
  } finally {
    poolStatsLoading.value = false
  }
}

const toggleAutoRefresh = (val) => {
  if (val) {
    refreshTimer = setInterval(loadPoolStats, 3000)
    ElMessage.success('已开启自动刷新 (3秒)')
  } else {
    if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
    ElMessage.info('已关闭自动刷新')
  }
}

const getUsageColor = (percent) => {
  if (percent < 50) return '#67c23a'
  if (percent < 80) return '#e6a23c'
  return '#f56c6c'
}

const formatDuration = (ms) => {
  if (!ms || ms <= 0) return '-'
  if (ms < 1000) return `${ms}ms`
  if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`
  if (ms < 3600000) return `${(ms / 60000).toFixed(1)}min`
  return `${(ms / 3600000).toFixed(1)}h`
}

onMounted(() => { loadData() })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped>
.el-card { border: none; border-radius: 16px; box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06); }
.stat-card { text-align: center; padding: 16px 0; border-radius: 12px; }
.stat-card .stat-value { font-size: 32px; font-weight: 700; line-height: 1.2; }
.stat-card .stat-label { font-size: 12px; color: #909399; margin-top: 8px; }
.stat-card-active { background: linear-gradient(135deg, #409eff15, #409eff05); }
.stat-card-active .stat-value { color: #409eff; }
.stat-card-idle { background: linear-gradient(135deg, #67c23a15, #67c23a05); }
.stat-card-idle .stat-value { color: #67c23a; }
.stat-card-total { background: linear-gradient(135deg, #e6a23c15, #e6a23c05); }
.stat-card-total .stat-value { color: #e6a23c; }
</style>
