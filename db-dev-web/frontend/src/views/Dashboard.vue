<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div class="welcome-info">
          <div class="welcome-header">
            <el-icon color="#fff" size="28"><Monitor /></el-icon>
            <h1>DB Dev 控制台</h1>
          </div>
          <p class="welcome-desc">
            面向开发者的数据库开发辅助工具，集成数据源管理、元数据浏览、SQL 执行与代码生成能力。
          </p>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="$router.push('/datasources')">
              <el-icon><Connection /></el-icon>
              数据源管理
            </el-button>
            <el-button type="primary" size="large" @click="$router.push('/tables')">
              <el-icon><Grid /></el-icon>
              表结构浏览
            </el-button>
            <el-button type="primary" size="large" @click="$router.push('/sql')">
              <el-icon><Lightning /></el-icon>
              SQL 执行器
            </el-button>
            <el-button type="primary" size="large" @click="$router.push('/codegen')">
              <el-icon><Tools /></el-icon>
              代码生成
            </el-button>
          </div>
        </div>
        <div class="welcome-decoration">
          <div class="decoration-circle c1"></div>
          <div class="decoration-circle c2"></div>
          <div class="decoration-circle c3"></div>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/datasources')">
          <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff);">
            <el-icon size="24"><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ datasourceStore.dataSources.length }}</div>
            <div class="stat-label">数据源</div>
          </div>
          <div class="stat-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/tables')">
          <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">
            <el-icon size="24"><Grid /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ tableCount }}</div>
            <div class="stat-label">数据表</div>
          </div>
          <div class="stat-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/sql')">
          <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563);">
            <el-icon size="24"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ sqlHistoryCount }}</div>
            <div class="stat-label">SQL 历史</div>
          </div>
          <div class="stat-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/druid')">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c, #f89898);">
            <el-icon size="24"><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ poolStatus }}</div>
            <div class="stat-label">连接池监控</div>
          </div>
          <div class="stat-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 数据源概览 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon color="#409eff"><Connection /></el-icon>
                <span>数据源概览</span>
              </div>
              <el-button type="primary" text size="small" @click="$router.push('/datasources')">
                管理数据源 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div v-if="datasourceStore.loading" v-loading="true" style="height: 200px;"></div>
          <div v-else-if="datasourceStore.dataSources.length === 0" class="empty-state">
            <el-empty description="暂无数据源">
              <el-button type="primary" @click="$router.push('/datasources')">
                添加数据源
              </el-button>
            </el-empty>
          </div>
          <div v-else class="datasource-list">
            <div 
              v-for="ds in datasourceStore.dataSources.slice(0, 5)" 
              :key="ds.name" 
              class="datasource-item"
              @click="$router.push('/tables')"
            >
              <div class="ds-icon" :style="{ background: getDsColor(ds.type) }">
                <el-icon><Database /></el-icon>
              </div>
              <div class="ds-info">
                <div class="ds-name">
                  <el-icon v-if="ds.primary" color="#f56c6c"><StarFilled /></el-icon>
                  {{ ds.name }}
                </div>
                <div class="ds-meta">
                  <el-tag size="small" :type="getDbTagType(ds.databaseType)">{{ ds.databaseType }}</el-tag>
                  <el-tag size="small" type="info">{{ ds.host }}:{{ ds.port }}</el-tag>
                </div>
              </div>
              <div class="ds-status">
                <el-tag type="success" size="small" effect="light">
                  <el-icon><CircleCheck /></el-icon>
                  已连接
                </el-tag>
              </div>
            </div>
            <div v-if="datasourceStore.dataSources.length > 5" class="more-item" @click="$router.push('/datasources')">
              <el-icon><Plus /></el-icon>
              查看更多 ({{ datasourceStore.dataSources.length - 5 }})
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧信息 -->
      <el-col :xs="24" :lg="10">
        <!-- 系统信息 -->
        <el-card shadow="hover" class="content-card system-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon color="#67c23a"><InfoFilled /></el-icon>
                <span>系统信息</span>
              </div>
            </div>
          </template>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="版本">v{{ projectVersion }}</el-descriptions-item>
            <el-descriptions-item label="环境">
              <el-tag size="small" type="success">{{ projectEnv }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 3</el-descriptions-item>
            <el-descriptions-item label="UI 组件">Element Plus</el-descriptions-item>
            <el-descriptions-item label="Java 版本">17+</el-descriptions-item>
            <el-descriptions-item label="Spring Boot">3.2.x</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 功能特性 -->
        <el-card shadow="hover" class="content-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon color="#e6a23c"><Star /></el-icon>
                <span>功能特性</span>
              </div>
            </div>
          </template>
          <div class="feature-grid">
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff);">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-name">多数据源管理</div>
                <div class="feature-desc">支持 MySQL、PostgreSQL 等多种数据库</div>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #f56c6c, #f89898);">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-name">连接池监控</div>
                <div class="feature-desc">实时监控 Druid 连接池状态</div>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">
                <el-icon><Grid /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-name">元数据浏览</div>
                <div class="feature-desc">查看表结构、字段、索引信息</div>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563);">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-name">SQL 执行器</div>
                <div class="feature-desc">在线执行 SQL 查询与结果导出</div>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #909399, #b4b4b8);">
                <el-icon><Tools /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-name">代码生成</div>
                <div class="feature-desc">一键生成 Entity、Mapper、Service</div>
              </div>
            </div>
            <div class="feature-item coming-soon">
              <div class="feature-icon" style="background: linear-gradient(135deg, #d4fc79, #96e6a1);">
                <el-icon><MagicStick /></el-icon>
              </div>
              <div class="feature-text">
                <div class="feature-name">AI 智能助手</div>
                <div class="feature-desc">AI 辅助 SQL 生成与优化</div>
              </div>
              <el-tag type="success" size="small" effect="dark">规划中</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDatasourceStore } from '@/stores/datasource'
import { getTableList } from '@/api/metadata'

const datasourceStore = useDatasourceStore()
const tableCount = ref(0)

// 项目版本和环境
const projectVersion = '1.0.0'
const projectEnv = 'Development'

const sqlHistoryCount = computed(() => {
  try {
    const history = localStorage.getItem('sql_executor_history')
    return history ? JSON.parse(history).length : 0
  } catch { return 0 }
})

const poolStatus = computed(() => {
  return datasourceStore.dataSources.length > 0 ? '活跃' : '未配置'
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
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.welcome-card {
  margin-bottom: 20px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #3b98ff 0%, #667eea 100%) !important;
  color: white;
  overflow: hidden;
}

:deep(.welcome-card .el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

:deep(.welcome-card .el-card__body) {
  padding: 24px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 40px;
  /* position: relative; */
  z-index: 1;
}

.welcome-info {
  flex: 1;
}

.welcome-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.welcome-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
}

.welcome-desc {
  font-size: 15px;
  line-height: 1.7;
  margin-bottom: 24px;
  opacity: 0.9;
  max-width: 600px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  backdrop-filter: blur(10px);
}

.quick-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.welcome-decoration {
  position: absolute;
  right: 40px;
  top: 50%;
  transform: translateY(-50%);
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.c1 {
  width: 200px;
  height: 200px;
  right: 80px;
  top: -60px;
}

.c2 {
  width: 120px;
  height: 120px;
  right: 20px;
  top: -100px;
}

.c3 {
  width: 60px;
  height: 60px;
  right: 150px;
  top: -40px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 20px;
  border: none;
  border-radius: 16px;
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card:hover .stat-arrow {
  opacity: 1;
  transform: translateX(0);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 12px;
}

.stat-info {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}

.stat-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%) translateX(10px);
  opacity: 0;
  transition: all 0.3s;
  color: #409eff;
}

.content-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  margin-bottom: 20px;
}

.content-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.system-card {
  margin-bottom: 20px;
}

.datasource-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.datasource-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8f9fb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.datasource-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.ds-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
}

.ds-info {
  flex: 1;
}

.ds-name {
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  font-size: 14px;
}

.ds-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.more-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #909399;
  cursor: pointer;
  transition: all 0.2s;
}

.more-item:hover {
  color: #409eff;
}

.empty-state {
  padding: 40px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  width: 100%;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #f8f9fb;
  border-radius: 12px;
  transition: all 0.3s;
  position: relative;
  width: 100%;
}

.feature-item:hover {
  background: #f0f2f5;
  transform: translateY(-2px);
}

.feature-item.coming-soon {
  opacity: 0.9;
}

.feature-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.feature-text {
  flex: 1;
  min-width: 0;
}

.feature-name {
  font-weight: 600;
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
}

.feature-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

@media (max-width: 1200px) {
  .welcome-decoration {
    display: none;
  }
}

@media (max-width: 992px) {
  .welcome-content {
    flex-direction: column;
    text-align: center;
  }

  .welcome-desc {
    max-width: 100%;
  }

  .quick-actions {
    justify-content: center;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-header h1 {
    font-size: 22px;
  }

  .welcome-desc {
    font-size: 14px;
  }

  .quick-actions .el-button {
    padding: 8px 16px;
    font-size: 13px;
  }

  .stat-value {
    font-size: 24px;
  }
}
</style>