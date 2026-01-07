<template>
  <div class="druid-monitor">
    <!-- 未启用 Druid 提示 -->
    <el-alert
      v-if="!druidEnabled && !loading"
      title="Druid 未启用"
      type="info"
      description="当前项目未使用 Alibaba Druid 数据源，此功能不可用。如需使用，请在项目中引入 druid-spring-boot-3-starter 依赖。"
      show-icon
      :closable="false"
    />

    <!-- Druid 监控内容 -->
    <template v-else-if="druidEnabled">
      <!-- Tab 导航 -->
      <el-tabs v-model="activeTab" type="border-card" class="druid-tabs">
        <!-- 连接池状态 Tab -->
        <el-tab-pane name="pool">
          <template #label>
            <span>
              <el-icon><Connection /></el-icon>
              连接池状态
            </span>
          </template>
          
          <PoolStats
            :pool-stats="poolStats"
            :loading="poolLoading"
            @refresh="loadPoolStats"
          />
        </el-tab-pane>

        <!-- SQL 统计 Tab -->
        <el-tab-pane name="sql">
          <template #label>
            <span>
              <el-icon><Document /></el-icon>
              SQL 统计
            </span>
          </template>
          
          <SqlStats
            ref="sqlStatsRef"
            :auto-refresh="activeTab === 'sql'"
            @loaded="handleSqlStatsLoaded"
          />
        </el-tab-pane>
      </el-tabs>
    </template>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { checkDruidEnabled, getPoolStats } from '@/api/druid'
import PoolStats from '@/components/PoolStats.vue'
import SqlStats from '@/components/SqlStats.vue'

const loading = ref(true)
const druidEnabled = ref(false)
const activeTab = ref('pool')
const poolStats = ref(null)
const poolLoading = ref(false)
const sqlStatsRef = ref(null)

let poolRefreshTimer = null

// 检查 Druid 是否启用
const checkEnabled = async () => {
  try {
    const res = await checkDruidEnabled()
    druidEnabled.value = res.data?.enabled || false
    if (druidEnabled.value) {
      await loadPoolStats()
    }
  } catch (error) {
    druidEnabled.value = false
    console.error('Failed to check Druid enabled:', error)
  } finally {
    loading.value = false
  }
}

// 加载连接池状态
const loadPoolStats = async () => {
  poolLoading.value = true
  try {
    const res = await getPoolStats()
    poolStats.value = res.data
  } catch (error) {
    console.error('Failed to load pool stats:', error)
  } finally {
    poolLoading.value = false
  }
}

// 处理 SQL 统计加载完成
const handleSqlStatsLoaded = (data) => {
  console.log('SQL stats loaded:', data.length, 'statements')
}

// 启动连接池自动刷新
const startPoolRefresh = () => {
  if (poolRefreshTimer) {
    clearInterval(poolRefreshTimer)
  }
  poolRefreshTimer = setInterval(() => {
    if (druidEnabled.value && activeTab.value === 'pool') {
      loadPoolStats()
    }
  }, 10000)
}

// 监听 Tab 切换
watch(activeTab, (newTab) => {
  if (newTab === 'sql' && sqlStatsRef.value) {
    sqlStatsRef.value.refresh()
  }
})

onMounted(() => {
  checkEnabled()
  startPoolRefresh()
})

onUnmounted(() => {
  if (poolRefreshTimer) {
    clearInterval(poolRefreshTimer)
  }
})
</script>

<style scoped>
.druid-monitor {
  padding: 10px;
}

.druid-tabs {
  min-height: calc(100vh - 100px);
  border-radius: 16px;
  overflow: hidden;
}

.druid-tabs :deep(.el-tabs__header) {
  border-radius: 0;
}

.druid-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
  font-size: 14px;
}

.druid-tabs :deep(.el-tabs__item span) {
  display: flex;
  align-items: center;
  gap: 6px;
}

.druid-tabs :deep(.el-tab-pane) {
  padding: 16px 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #909399;
}

.loading-container .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

@media (max-width: 768px) {
  .druid-monitor {
    padding: 0;
  }
  
  .druid-tabs {
    min-height: auto;
    border-radius: 0;
  }
  
  .druid-tabs :deep(.el-tabs__item) {
    height: 44px;
    line-height: 44px;
    padding: 0 16px;
  }
}
</style>