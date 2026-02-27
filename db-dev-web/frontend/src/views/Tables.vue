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
            <el-button type="primary" size="small" @click="openCreateDialog" :disabled="!selectedDataSource">
              <el-icon><Plus /></el-icon>
              新建表
            </el-button>
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

    <!-- 创建表弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建表"
      width="1200px"
      destroy-on-close
      :close-on-click-modal="false"
      class="create-table-dialog">
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createFormRules"
        label-width="80px"
        status-icon>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="表名" prop="tableName">
              <el-input
                v-model="createForm.tableName"
                placeholder="例如: user"
                clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remarks">
              <el-input
                v-model="createForm.remarks"
                placeholder="表用途说明"
                clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="字段定义" required>
          <div class="field-table-wrapper">
            <el-table
              :data="createForm.columns"
              size="small"
              border
              style="width: 100%; margin-bottom: 8px;">
              <el-table-column prop="columnName" label="字段名" min-width="120">
                <template #default="{ row }">
                  <el-input v-model="row.columnName" placeholder="例如: id" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="typeName" label="类型" min-width="120">
                <template #default="{ row }">
                  <el-select v-model="row.typeName" placeholder="类型" size="small" style="width: 110px;">
                    <el-option
                      v-for="t in columnTypeOptions"
                      :key="t.value"
                      :label="t.label"
                      :value="t.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column prop="columnSize" label="长度" width="80">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.columnSize"
                    :min="1"
                    :max="65535"
                    size="small"
                    controls-position="right"
                    style="width: 100%;" />
                </template>
              </el-table-column>
              <el-table-column prop="decimalDigits" label="小数" width="80">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.decimalDigits"
                    :min="0"
                    :max="10"
                    size="small"
                    controls-position="right"
                    style="width: 100%;" />
                </template>
              </el-table-column>
              <el-table-column prop="nullable" label="可空" width="70" align="center">
                <template #default="{ row }">
                  <el-switch v-model="row.nullable" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="primaryKey" label="主键" width="70" align="center">
                <template #default="{ row }">
                  <el-switch v-model="row.primaryKey" size="small" @change="handlePrimaryKeyChange(row)" />
                </template>
              </el-table-column>
              <el-table-column prop="autoIncrement" label="自增" width="70" align="center">
                <template #default="{ row }">
                  <el-switch v-model="row.autoIncrement" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="defaultValue" label="默认值" min-width="120">
                <template #default="{ row }">
                  <el-input
                    v-model="row.defaultValue"
                    size="small"
                    placeholder="例如: 0, CURRENT_TIMESTAMP" />
                </template>
              </el-table-column>
              <el-table-column prop="remarks" label="备注" min-width="120">
                <template #default="{ row }">
                  <el-input v-model="row.remarks" size="small" placeholder="字段说明" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" fixed="right" align="center">
                <template #default="{ $index }">
                  <el-button
                    type="danger"
                    link
                    size="small"
                    @click="removeColumn($index)"
                    :disabled="createForm.columns.length <= 1">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-button type="primary" plain size="small" @click="addColumn">
              <el-icon><Plus /></el-icon>
              新增字段
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="creatingTable" @click="submitCreateTable">
            确 定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { useDatasourceStore } from '@/stores/datasource'
import { getTableList, getTableDetail, createTable } from '@/api/metadata'
import { exportSchema } from '@/api/export'
import TableDetail from '@/components/TableDetail.vue'

const datasourceStore = useDatasourceStore()

const selectedDataSource = ref('')
const tables = ref([])
const tableSearch = ref('')
const loadingTables = ref(false)
const selectedTable = ref(null)

const createDialogVisible = ref(false)
const createFormRef = ref(null)
const creatingTable = ref(false)
const createForm = ref({
  tableName: '',
  remarks: '',
  columns: [
    {
      columnName: 'id',
      typeName: 'BIGINT',
      columnSize: 20,
      decimalDigits: 0,
      nullable: false,
      defaultValue: null,
      remarks: '主键',
      autoIncrement: true,
      primaryKey: true
    }
  ]
})

const columnTypeOptions = [
  { label: 'BIGINT', value: 'BIGINT' },
  { label: 'INT', value: 'INT' },
  { label: 'SMALLINT', value: 'SMALLINT' },
  { label: 'TINYINT', value: 'TINYINT' },
  { label: 'BOOLEAN', value: 'BOOLEAN' },
  { label: 'CHAR', value: 'CHAR' },
  { label: 'VARCHAR', value: 'VARCHAR' },
  { label: 'TEXT', value: 'TEXT' },
  { label: 'LONGTEXT', value: 'LONGTEXT' },
  { label: 'DECIMAL', value: 'DECIMAL' },
  { label: 'DOUBLE', value: 'DOUBLE' },
  { label: 'FLOAT', value: 'FLOAT' },
  { label: 'DATE', value: 'DATE' },
  { label: 'TIME', value: 'TIME' },
  { label: 'DATETIME', value: 'DATETIME' },
  { label: 'TIMESTAMP', value: 'TIMESTAMP' }
]

const createFormRules = {
  tableName: [
    { required: true, message: '请输入表名', trigger: 'blur' },
    { pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: '表名需以字母/下划线开头，只能包含字母数字下划线', trigger: 'blur' }
  ]
}

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

const openCreateDialog = () => {
  if (!selectedDataSource.value) {
    ElMessage.warning('请先选择数据源')
    return
  }
  // 重置表单
  createForm.value = {
    tableName: '',
    remarks: '',
    columns: [
      {
        columnName: 'id',
        typeName: 'BIGINT',
        columnSize: 20,
        decimalDigits: 0,
        nullable: false,
        defaultValue: null,
        remarks: '主键',
        autoIncrement: true,
        primaryKey: true
      }
    ]
  }
  createDialogVisible.value = true
}

const addColumn = () => {
  createForm.value.columns.push({
    columnName: '',
    typeName: 'VARCHAR',
    columnSize: 255,
    decimalDigits: 0,
    nullable: true,
    defaultValue: null,
    remarks: '',
    autoIncrement: false,
    primaryKey: false
  })
}

const removeColumn = (index) => {
  if (createForm.value.columns.length <= 1) {
    ElMessage.warning('至少需要一个字段')
    return
  }
  createForm.value.columns.splice(index, 1)
}

const handlePrimaryKeyChange = (row) => {
  if (row.primaryKey) {
    // 仅允许单主键，取消其他字段主键标记
    createForm.value.columns.forEach(col => {
      if (col !== row) {
        col.primaryKey = false
      }
    })
    // 主键一般不可为空
    row.nullable = false
  }
}

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

const validateColumns = () => {
  if (!createForm.value.columns || createForm.value.columns.length === 0) {
    ElMessage.error('请至少添加一个字段')
    return false
  }

  for (const col of createForm.value.columns) {
    if (!col.columnName || !/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(col.columnName)) {
      ElMessage.error('字段名需以字母/下划线开头，只能包含字母数字下划线')
      return false
    }
    if (!col.typeName) {
      ElMessage.error(`字段 "${col.columnName || ''}" 类型不能为空`)
      return false
    }
  }
  return true
}

const submitCreateTable = () => {
  if (!selectedDataSource.value) {
    ElMessage.warning('请先选择数据源')
    return
  }
  if (!validateColumns()) {
    return
  }

  createFormRef.value?.validate(async (valid) => {
    if (!valid) return
    creatingTable.value = true
    try {
      await createTable(selectedDataSource.value, createForm.value)
      ElMessage.success('创建表成功')
      createDialogVisible.value = false
      await loadTables()
    } catch (error) {
      // 错误提示已在全局拦截器中处理，这里只保持 loading 状态正确
    } finally {
      creatingTable.value = false
    }
  })
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

.create-table-dialog :deep(.el-dialog__body) {
  padding-top: 12px;
  padding-bottom: 16px;
}

.field-table-wrapper {
  width: 100%;
  max-height: 380px;
  padding: 4px 0;
  overflow: auto;
  border-radius: 8px;
  background: linear-gradient(180deg, #f5f7fa, #ffffff);
}

.field-table-wrapper :deep(.el-table) {
  background-color: transparent;
}

.field-table-wrapper :deep(.el-table__header),
.field-table-wrapper :deep(.el-table__body) {
  background-color: transparent;
}
</style>
