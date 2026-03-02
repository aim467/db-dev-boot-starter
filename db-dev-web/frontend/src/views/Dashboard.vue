<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-main">
        <div class="welcome-title">
          <el-icon size="32" color="#409eff"><Monitor /></el-icon>
          <h1>DB Dev 控制台</h1>
        </div>
        <p class="welcome-subtitle">
          面向开发者的数据库开发辅助工具，集成数据源管理、元数据浏览、SQL 执行与代码生成能力
        </p>
      </div>

      <!-- 动态时间显示 -->
      <div class="welcome-time">
        <div class="time-display">
          <el-icon size="20"><Clock /></el-icon>
          <span class="time-value">{{ currentTime }}</span>
        </div>
        <div class="date-display">{{ currentDate }}</div>
      </div>
    </div>

    <!-- 快捷工具栏 -->
    <div class="quick-toolbar">
      <div class="toolbar-item" @click="$router.push('/datasources')">
        <div class="toolbar-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff);">
          <el-icon><Connection /></el-icon>
        </div>
        <span class="toolbar-label">数据源</span>
      </div>
      <div class="toolbar-divider"></div>
      <div class="toolbar-item" @click="$router.push('/tables')">
        <div class="toolbar-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">
          <el-icon><Grid /></el-icon>
        </div>
        <span class="toolbar-label">表结构</span>
      </div>
      <div class="toolbar-divider"></div>
      <div class="toolbar-item" @click="$router.push('/sql')">
        <div class="toolbar-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563);">
          <el-icon><Lightning /></el-icon>
        </div>
        <span class="toolbar-label">SQL执行</span>
      </div>
      <div class="toolbar-divider"></div>
      <div class="toolbar-item" @click="$router.push('/codegen')">
        <div class="toolbar-icon" style="background: linear-gradient(135deg, #909399, #b4b4b8);">
          <el-icon><Tools /></el-icon>
        </div>
        <span class="toolbar-label">代码生成</span>
      </div>
      <div class="toolbar-divider"></div>
      <div class="toolbar-item" @click="$router.push('/druid')">
        <div class="toolbar-icon" style="background: linear-gradient(135deg, #f56c6c, #f89898);">
          <el-icon><Cpu /></el-icon>
        </div>
        <span class="toolbar-label">监控中心</span>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧：数据源详情 -->
      <div class="content-left">
        <el-card shadow="hover" class="datasource-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon color="#409eff"><Connection /></el-icon>
                <span>数据源概览</span>
              </div>
              <el-button type="primary" link @click="$router.push('/datasources')">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>

          <div v-if="datasourceStore.loading" v-loading="true" style="height: 200px;"></div>
          <div v-else-if="datasourceStore.dataSources.length === 0" class="empty-state">
            <el-empty description="暂无数据源">
              <el-button type="primary" @click="$router.push('/datasources')">添加数据源</el-button>
            </el-empty>
          </div>
          <div v-else class="datasource-list">
            <div
              v-for="ds in datasourceStore.dataSources.slice(0, 6)"
              :key="ds.name"
              class="datasource-item"
              :class="{ primary: ds.primary }"
              @click="$router.push('/tables')"
            >
              <div class="ds-left">
                <div class="ds-icon" :style="{ background: getDsColor(ds.type) }">
                  <el-icon><Connection /></el-icon>
                </div>
                <div class="ds-info">
                  <div class="ds-name">
                    {{ ds.name }}
                    <el-tag v-if="ds.primary" size="small" type="primary" effect="light">主</el-tag>
                  </div>
                  <div class="ds-address">{{ ds.url }}</div>
                </div>
              </div>
              <div class="ds-right">
                <el-tag size="small" :type="getDbTagType(ds.databaseType)">{{ ds.databaseType }}</el-tag>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </el-card>

      </div>

      <!-- 右侧：系统状态 -->
      <div class="content-right">
        <el-card shadow="hover" class="status-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon color="#67c23a"><InfoFilled /></el-icon>
                <span>系统状态</span>
              </div>
              <el-button type="primary" link :loading="systemLoading" @click="loadSystemInfo">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="systemLoading">
            <el-empty v-if="!systemLoading && !systemInfo" description="暂无信息" />
            <div v-else-if="systemInfo" class="status-list">
              <div class="status-item">
                <span class="status-label">运行状态</span>
                <el-tag size="small" :type="systemInfo.status === 'running' ? 'success' : 'danger'">
                  {{ systemInfo.status === 'running' ? '运行中' : '异常' }}
                </el-tag>
              </div>
              <div class="status-item">
                <span class="status-label">应用版本</span>
                <span class="status-value">{{ systemInfo.version }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">Spring Boot</span>
                <span class="status-value">{{ systemInfo.springBootVersion }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">Java 版本</span>
                <span class="status-value">{{ systemInfo.javaVersion }}</span>
              </div>
              <div class="status-item">
                <span class="status-label">操作系统</span>
                <span class="status-value">{{ systemInfo.osName }} {{ systemInfo.osArch }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 连接池状态（独立于主内容区） -->
    <div class="pool-section">
      <el-card shadow="hover" class="pool-card">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon color="#e6a23c"><DataLine /></el-icon>
              <span>连接池概览</span>
            </div>
          </div>
        </template>
        <div class="pool-content">
          <div class="pool-stats-row">
            <div class="pool-stat-item">
              <div class="pool-icon-large" style="background: linear-gradient(135deg, #ecf5ff, #d9ecff); color: #409eff;">
                <el-icon size="28"><Link /></el-icon>
              </div>
              <div class="pool-stat-info">
                <span class="pool-stat-value">{{ poolStatus }}</span>
                <span class="pool-stat-label">连接池状态</span>
              </div>
            </div>
            <div class="pool-stat-item">
              <div class="pool-icon-large" style="background: linear-gradient(135deg, #f0f9eb, #e1f3d8); color: #67c23a;">
                <el-icon size="28"><Check /></el-icon>
              </div>
              <div class="pool-stat-info">
                <span class="pool-stat-value">{{ datasourceStore.dataSources.length }}</span>
                <span class="pool-stat-label">活跃数据源</span>
              </div>
            </div>
            <div class="pool-stat-item">
              <div class="pool-icon-large" style="background: linear-gradient(135deg, #fdf6ec, #faecd8); color: #e6a23c;">
                <el-icon size="28"><Timer /></el-icon>
              </div>
              <div class="pool-stat-info">
                <span class="pool-stat-value">--</span>
                <span class="pool-stat-label">活跃连接数</span>
              </div>
            </div>
            <div class="pool-stat-item pool-link-item" @click="$router.push('/druid')">
              <div class="pool-icon-large" style="background: linear-gradient(135deg, #fef0f0, #fde2e2); color: #f56c6c;">
                <el-icon size="28"><TrendCharts /></el-icon>
              </div>
              <div class="pool-stat-info">
                <span class="pool-stat-value">详情</span>
                <span class="pool-stat-label">查看监控图表</span>
              </div>
              <el-icon class="pool-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useDatasourceStore } from '@/stores/datasource'
import { getTableList } from '@/api/metadata'
import { getSystemInfo } from '@/api/system'

const datasourceStore = useDatasourceStore()
const tableCount = ref(0)
const systemInfo = ref(null)
const systemLoading = ref(false)
const currentTime = ref('')
const currentDate = ref('')
let timeInterval = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

const startTimeUpdate = () => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
}

const stopTimeUpdate = () => {
  if (timeInterval) {
    clearInterval(timeInterval)
    timeInterval = null
  }
}

const sqlHistoryCount = computed(() => {
  try {
    const history = localStorage.getItem('sql_executor_history')
    return history ? JSON.parse(history).length : 0
  } catch { return 0 }
})

const poolStatus = computed(() => {
  return datasourceStore.dataSources.length > 0 ? '正常' : '未配置'
})

const getDsColor = (type) => {
  const colors = {
    'mysql': 'linear-gradient(135deg, #4479a1, #5a9bd4)',
    'postgresql': 'linear-gradient(135deg, #336791, #4479a1)',
    'oracle': 'linear-gradient(135deg, #c74634, #e05842)',
    'sqlserver': 'linear-gradient(135deg, #cc2927, #e33e3c)'
  }
  return colors[type?.toLowerCase()] || 'linear-gradient(135deg, #909399, #b4b4b8)'
}

const getDbTagType = (type) => {
  const types = {
    'mysql': '',
    'postgresql': 'success',
    'oracle': 'warning',
    'sqlserver': 'danger'
  }
  return types[type?.toLowerCase()] || 'info'
}

const loadSystemInfo = async () => {
  systemLoading.value = true
  try {
    const res = await getSystemInfo()
    systemInfo.value = res.data || null
  } catch (e) {
    console.error('加载系统信息失败', e)
  } finally {
    systemLoading.value = false
  }
}

const loadData = async () => {
  try {
    await datasourceStore.loadDataSources()
    if (datasourceStore.dataSources.length > 0) {
      const primaryDs = datasourceStore.dataSources.find(ds => ds.primary) || datasourceStore.dataSources[0]
      const res = await getTableList(primaryDs.name)
      tableCount.value = res.data?.length || 0
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

onMounted(() => {
  loadData()
  loadSystemInfo()
  startTimeUpdate()
})

onUnmounted(() => {
  stopTimeUpdate()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #3b98ff 0%, #667eea 100%);
  border-radius: 12px;
  padding: 24px 32px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.welcome-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.welcome-title h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.welcome-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.welcome-stats-mini {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.15);
  padding: 12px 24px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.mini-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.mini-value {
  font-size: 20px;
  font-weight: 700;
}

.mini-label {
  font-size: 12px;
  opacity: 0.8;
}

.mini-divider {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.3);
}

/* 动态时间显示 */
.welcome-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255, 255, 255, 0.15);
  padding: 12px 24px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
  min-width: 140px;
}

.time-display {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  line-height: 1;
}

.date-display {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 6px;
}

/* 快捷工具栏 */
.quick-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  background: #fff;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  gap: 8px;
}

.toolbar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.toolbar-item:hover {
  background: #f5f7fa;
  transform: translateY(-2px);
}

.toolbar-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.toolbar-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: #e4e7ed;
}

/* 主内容区 */
.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.content-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片样式 */
:deep(.el-card) {
  border: none;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-card__body) {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

/* 数据源列表 */
.datasource-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.datasource-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #f8f9fb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.datasource-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
}

.datasource-item.primary {
  background: #fef0f0;
  border-color: #419ae3;
}

.ds-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ds-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.ds-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ds-name {
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.ds-address {
  font-size: 12px;
  color: #909399;
}

.ds-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.arrow-icon {
  color: #c0c4cc;
  transition: all 0.2s;
}

.datasource-item:hover .arrow-icon {
  color: #409eff;
  transform: translateX(4px);
}

.empty-state {
  padding: 40px;
}

/* 系统状态 */
.status-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.status-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.status-label {
  font-size: 13px;
  color: #606266;
}

.status-value {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

/* 连接池概览 - 独立区域 */
.pool-section {
  margin-top: 20px;
}

.pool-content {
  padding: 8px;
}

.pool-stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.pool-stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8f9fb;
  border-radius: 12px;
  transition: all 0.3s;
}

.pool-stat-item:hover {
  background: #f0f2f5;
  transform: translateY(-2px);
}

.pool-stat-item.pool-link-item {
  cursor: pointer;
  position: relative;
}

.pool-stat-item.pool-link-item:hover {
  background: #ecf5ff;
}

.pool-icon-large {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pool-stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.pool-stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.pool-stat-label {
  font-size: 13px;
  color: #909399;
}

.pool-arrow {
  color: #c0c4cc;
  transition: all 0.2s;
}

.pool-link-item:hover .pool-arrow {
  color: #409eff;
  transform: translateX(4px);
}

:deep(.el-divider) {
  margin: 16px 0;
}

.pool-link {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  color: #409eff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.pool-link:hover {
  color: #66b1ff;
}

/* 响应式 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }

  .pool-stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .quick-toolbar {
    flex-wrap: wrap;
    gap: 4px;
  }

  .toolbar-divider {
    display: none;
  }

  .toolbar-item {
    flex: 1;
    min-width: 120px;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    padding: 20px;
  }

  .welcome-title {
    justify-content: center;
  }

  .welcome-stats-mini {
    width: 100%;
    justify-content: center;
  }

  .welcome-time {
    width: 100%;
  }

  .time-display {
    font-size: 20px;
  }

  .quick-toolbar {
    padding: 12px 16px;
  }

  .toolbar-item {
    padding: 8px 12px;
    min-width: 100px;
  }

  .toolbar-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }

  .toolbar-label {
    font-size: 13px;
  }

  .pool-stats-row {
    grid-template-columns: 1fr;
  }

  .pool-stat-item {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .quick-toolbar {
    flex-direction: column;
    gap: 8px;
  }

  .toolbar-item {
    width: 100%;
    justify-content: flex-start;
    padding: 12px 16px;
  }

  .pool-icon-large {
    width: 48px;
    height: 48px;
  }
}
</style>