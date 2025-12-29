<template>
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
      <el-empty 
        v-if="!datasourceStore.loading && datasourceStore.dataSources.length === 0" 
        description="未找到数据源">
        <el-button type="primary" size="small">添加数据源</el-button>
      </el-empty>
      <el-table 
        v-else 
        :data="datasourceStore.dataSources" 
        stripe 
        style="width: 100%" 
        height="500">
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
            <el-text type="info" size="small" style="font-family: monospace;">
              {{ scope.row.url }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status || 'success'" size="small">
              <el-icon><CircleCheck /></el-icon>
              {{ scope.row.status === 'error' ? '连接失败' : '已连接' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button type="primary" size="small" @click="testConnection(scope.row)">
                <el-icon><Link /></el-icon>
              </el-button>
              <el-button type="warning" size="small" @click="editDataSource(scope.row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" size="small" @click="deleteDataSource(scope.row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { useDatasourceStore } from '@/stores/datasource'
import { testConnection as testConnectionApi, deleteDatasource } from '@/api/datasource'

const datasourceStore = useDatasourceStore()

const getDatabaseTypeColor = (type) => {
  const typeMap = {
    'MySQL': 'success',
    'PostgreSQL': 'warning',
    'Oracle': 'danger',
    'SQL Server': 'info'
  }
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
  const loading = ElLoading.service({
    lock: true,
    text: '正在测试所有连接...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const promises = datasourceStore.dataSources.map(ds => testConnection(ds))
    await Promise.all(promises)
    ElMessage.success('所有连接测试完成')
  } finally {
    loading.close()
  }
}

const editDataSource = (dataSource) => {
  ElMessage.info(`编辑数据源: ${dataSource.name}`)
}

const deleteDataSource = (dataSource) => {
  ElMessageBox.confirm(
    `确定要删除数据源 "${dataSource.name}" 吗？`,
    '提示',
    { type: 'warning' }
  ).then(async () => {
    try {
      await deleteDatasource(dataSource.name)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败: ' + error.message)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}
</style>
