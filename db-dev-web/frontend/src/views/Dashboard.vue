<template>
  <div>
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="hover">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon color="#fff"><Promotion /></el-icon>
          <span style="font-weight: 600; color: #fff;">欢迎使用 DB Dev</span>
        </div>
      </template>
      <p style="margin-bottom: 15px; color: #fff; font-size: 14px; line-height: 1.6;">
        面向开发者的数据库开发辅助工具，集成数据源管理、元数据浏览、SQL 执行与代码生成能力。
      </p>
      <div class="quick-actions">
        <el-button type="primary" plain @click="$router.push('/datasources')">
          <el-icon><Connection /></el-icon> 数据源管理
        </el-button>
        <el-button type="primary" plain @click="$router.push('/tables')">
          <el-icon><Folder /></el-icon> 表结构浏览
        </el-button>
        <el-button type="primary" plain @click="$router.push('/sql')">
          <el-icon><Lightning /></el-icon> SQL 执行器
        </el-button>
        <el-button type="primary" plain @click="$router.push('/codegen')">
          <el-icon><Tools /></el-icon> 代码生成
        </el-button>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/datasources')">
          <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff);">
            <el-icon size="28"><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ datasourceStore.dataSources.length }}</div>
            <div class="stat-label">数据源</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/tables')">
          <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">
            <el-icon size="28"><Grid /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ tableCount }}</div>
            <div class="stat-label">数据表</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/sql')">
          <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563);">
            <el-icon size="28"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ sqlHistoryCount }}</div>
            <div class="stat-label">SQL 历史</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c, #f89898);">
            <el-icon size="28"><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ poolStatus }}</div>
            <div class="stat-label">连接池状态</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 数据源概览 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-icon color="#409eff"><Connection /></el-icon>
                <span style="font-weight: 600;">数据源概览</span>
              </div>
              <el-button type="primary" text size="small" @click="$router.push('/datasources')">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-if="datasourceStore.loading" v-loading="true" style="height: 200px;"></div>
          <div v-else-if="datasourceStore.dataSources.length === 0" style="text-align: center; padding: 40px; color: #909399;">
            <el-icon size="48"><Connection /></el-icon>
            <p>暂无数据源</p>
          </div>
          <div v-else class="datasource-list">
            <div v-for="ds in datasourceStore.dataSources.slice(0, 4)" :key="ds.name" class="datasource-item" @click="$router.push('/datasources')">
              <div class="ds-info">
                <div class="ds-name">
                  <el-icon v-if="ds.primary" color="#f56c6c"><Star /></el-icon>
                  {{ ds.name }}
                </div>
                <div class="ds-meta">
                  <el-tag size="small" type="info">{{ ds.databaseType }}</el-tag>
                  <el-tag size="small">{{ ds.type }}</el-tag>
                </div>
              </div>
              <el-tag type="success" size="small">
                <el-icon><CircleCheck /></el-icon>
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧信息 -->
      <el-col :span="10">
        <!-- 系统信息 -->
        <el-card shadow="hover" style="margin-bottom: 20px;">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon color="#67c23a"><InfoFilled /></el-icon>
              <span style="font-weight: 600;">系统信息</span>
            </div>
          </template>
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="版本">1.0.0-SNAPSHOT</el-descriptions-item>
            <el-descriptions-item label="环境">Development</el-descriptions-item>
            <el-descriptions-item label="Java">{{ javaVersion }}</el-descriptions-item>
            <el-descriptions-item label="Spring Boot">3.x</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 功能特性 -->
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon color="#e6a23c"><Star /></el-icon>
              <span style="font-weight: 600;">功能特性</span>
            </div>
          </template>
          <ul class="feature-list-simple">
            <li><el-icon color="#67c23a"><Check /></el-icon> 多数据源管理</li>
            <li><el-icon color="#67c23a"><Check /></el-icon> 连接池实时监控</li>
            <li><el-icon color="#67c23a"><Check /></el-icon> 表结构元数据浏览</li>
            <li><el-icon color="#67c23a"><Check /></el-icon> SQL 在线执行</li>
            <li><el-icon color="#67c23a"><Check /></el-icon> 代码一键生成</li>
            <li><el-icon color="#909399"><Clock /></el-icon> AI 辅助 (规划中)</li>
          </ul>
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
const javaVersion = ref('-')

const sqlHistoryCount = computed(() => {
  try {
    const history = localStorage.getItem('sql_executor_history')
    return history ? JSON.parse(history).length : 0
  } catch { return 0 }
})

const poolStatus = computed(() => {
  return datasourceStore.dataSources.length > 0 ? '正常' : '-'
})

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
  javaVersion.value = '17+'
})
</script>

<style scoped>
.welcome-card {
  margin-bottom: 20px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #3b98ff 0%, #667eea 100%) !important;
  color: white;
  overflow: hidden;
  position: relative;
}
.welcome-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}
:deep(.welcome-card .el-card__header) {
  background: rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}
:deep(.welcome-card .el-card__body) {
  position: relative;
  z-index: 1;
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
}
.quick-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.stat-card {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 20px;
  border: none;
  border-radius: 16px;
  text-align: center;
}
.stat-card:hover {
  transform: translateY(-4px);
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 16px;
}
.stat-info { text-align: center; }
.stat-value { font-size: 32px; font-weight: 700; color: #303133; line-height: 1; }
.stat-label { font-size: 13px; color: #909399; margin-top: 8px; }

.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}
.el-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.datasource-list { display: flex; flex-direction: column; gap: 12px; }
.datasource-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.datasource-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}
.ds-name {
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}
.ds-meta { display: flex; gap: 8px; }

.feature-list-simple {
  list-style: none;
  padding: 0;
  margin: 0;
}
.feature-list-simple li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  color: #606266;
  font-size: 14px;
  border-bottom: 1px dashed #ebeef5;
}
.feature-list-simple li:last-child {
  border-bottom: none;
}
</style>
