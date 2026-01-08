<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon><Setting /></el-icon>
          <span style="font-weight: 600;">数据源选择</span>
        </div>
      </template>
      <el-select
        v-model="selectedDataSource"
        placeholder="请选择数据源"
        style="width: 300px;"
        @change="loadTables"
        clearable>
        <el-option
          v-for="ds in datasourceStore.dataSources"
          :key="ds.name"
          :label="ds.name"
          :value="ds.name">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon v-if="ds.primary" color="#f56c6c"><Star /></el-icon>
            <span>{{ ds.name }}</span>
            <el-tag size="small" :type="getDatabaseTypeColor(ds.databaseType)">
              {{ ds.databaseType }}
            </el-tag>
          </div>
        </el-option>
      </el-select>
    </el-card>

    <el-card v-if="selectedDataSource" shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon><Folder /></el-icon>
            <span style="font-weight: 600;">表列表</span>
            <el-tag v-if="tables.length > 0" type="info" size="small">{{ tables.length }}</el-tag>
          </div>
          <div style="display: flex; gap: 8px;">
            <el-input
              v-model="tableSearch"
              placeholder="搜索表名"
              style="width: 200px;"
              clearable>
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" size="small" @click="loadTables" :loading="loadingTables">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-dropdown trigger="click" @command="handleExport">
              <el-button type="success" size="small">
                <el-icon><Download /></el-icon>
                导出
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="markdown">
                    <el-icon><Document /></el-icon>
                    Markdown 文档
                  </el-dropdown-item>
                  <el-dropdown-item command="html">
                    <el-icon><Document /></el-icon>
                    HTML 网页
                  </el-dropdown-item>
                  <el-dropdown-item command="pdf">
                    <el-icon><Document /></el-icon>
                    PDF 文档
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
      <div v-loading="loadingTables">
        <el-empty v-if="!loadingTables && filteredTables.length === 0" description="未找到表" />
        <el-table v-else :data="filteredTables" stripe style="width: 100%" height="400">
          <el-table-column prop="tableName" label="表名" min-width="200" sortable>
            <template #default="scope">
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-icon color="#409eff"><Document /></el-icon>
                <strong>{{ scope.row.tableName }}</strong>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="tableType" label="类型" width="120" sortable>
            <template #default="scope">
              <el-tag :type="scope.row.tableType === 'TABLE' ? 'success' : 'info'" size="small">
                {{ scope.row.tableType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remarks" label="备注" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.remarks || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="columnCount" label="字段数" width="100" sortable>
            <template #default="scope">
              <el-tag size="small">{{ scope.row.columnCount || 0 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewTableDetail(scope.row.tableName)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <TableDetail 
      v-if="selectedTable" 
      :table="selectedTable" 
      @close="selectedTable = null" 
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { useDatasourceStore } from '@/stores/datasource'
import { getTableList, getTableDetail } from '@/api/metadata'
import { exportSchema } from '@/api/export'
import TableDetail from '@/components/TableDetail.vue'

const datasourceStore = useDatasourceStore()

const selectedDataSource = ref('')
const tables = ref([])
const tableSearch = ref('')
const loadingTables = ref(false)
const selectedTable = ref(null)

const getDatabaseTypeColor = (type) => {
  const typeMap = {
    'MySQL': 'success',
    'PostgreSQL': 'warning',
    'Oracle': 'danger',
    'SQL Server': 'info'
  }
  return typeMap[type] || 'info'
}

const filteredTables = computed(() => {
  if (!tableSearch.value) return tables.value
  const search = tableSearch.value.toLowerCase()
  return tables.value.filter(t => t.tableName.toLowerCase().includes(search))
})

const loadTables = async () => {
  if (!selectedDataSource.value) return

  loadingTables.value = true
  selectedTable.value = null
  tableSearch.value = ''
  try {
    const res = await getTableList(selectedDataSource.value)
    tables.value = res.data
    ElMessage.success(`成功加载 ${tables.value.length} 个表`)
  } catch (error) {
    ElMessage.error('加载表列表失败: ' + error.message)
  } finally {
    loadingTables.value = false
  }
}

const viewTableDetail = async (tableName) => {
  const loading = ElLoading.service({
    lock: true,
    text: '正在加载表详情...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  try {
    const res = await getTableDetail(tableName, selectedDataSource.value)
    selectedTable.value = res.data
    ElMessage.success('加载表详情成功')
  } catch (error) {
    ElMessage.error('加载表详情失败: ' + error.message)
  } finally {
    loading.close()
  }
}

onMounted(async () => {
  if (datasourceStore.dataSources.length === 0) {
    await datasourceStore.loadDataSources()
  }
})

// 导出表结构文档
const handleExport = async (format) => {
  if (!selectedDataSource.value) {
    ElMessage.warning('请先选择数据源')
    return
  }

  try {
    ElMessage.info('正在导出...')

    // PDF 导出：前端使用 html2pdf.js 生成
    if (format === 'pdf') {
      // 先获取 HTML 内容（Blob 格式）
      const res = await exportSchema(selectedDataSource.value, 'html')
      const htmlContent = await res.data.text()

      // 使用 html2pdf.js 生成 PDF
      const element = document.createElement('div')
      element.innerHTML = htmlContent
      element.style.width = '800px'
      element.style.padding = '20px'
      element.style.fontFamily = 'Arial, sans-serif'

      const opt = {
        margin: [10, 10, 10, 10],
        filename: `schema_${selectedDataSource.value}_${Date.now()}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
      }

      // 等待 html2pdf 加载完成
      await new Promise((resolve) => setTimeout(resolve, 100))

      window.html2pdf().set(opt).from(element).save()
      ElMessage.success('PDF 导出成功')
      return
    }

    // Markdown 或 HTML 导出
    const res = await exportSchema(selectedDataSource.value, format)

    // 获取文件名
    const contentDisposition = res.headers?.['content-disposition'] || res.headers?.Content-Disposition
    let fileName = `schema_${selectedDataSource.value}_${Date.now()}`
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="(.+)"/)
      if (match && match[1]) {
        fileName = match[1]
      }
    }
    if (!fileName.endsWith('.md') && !fileName.endsWith('.html')) {
      fileName += format === 'markdown' ? '.md' : '.html'
    }

    // 创建下载链接
    const blob = new Blob([res.data], {
      type: format === 'markdown' ? 'text/markdown' : 'text/html'
    })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = fileName
    link.click()
    window.URL.revokeObjectURL(link.href)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}
</style>
