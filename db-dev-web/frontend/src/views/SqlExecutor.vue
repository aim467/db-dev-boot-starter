<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon><Lightning /></el-icon>
            <span style="font-weight: 600;">SQL 执行器</span>
          </div>
          <div style="display: flex; gap: 8px;">
            <el-select
              v-model="sqlDataSource"
              placeholder="选择数据源"
              style="width: 200px;"
              clearable>
              <el-option
                v-for="ds in datasourceStore.dataSources"
                :key="ds.name"
                :label="ds.name"
                :value="ds.name">
                <div style="display: flex; align-items: center; gap: 8px;">
                  <el-icon v-if="ds.primary" color="#f56c6c"><Star /></el-icon>
                  <span>{{ ds.name }}</span>
                </div>
              </el-option>
            </el-select>
            <el-button type="primary" @click="executeSqlQuery" :loading="executingSql" :disabled="!sqlDataSource || !sqlText">
              <el-icon><CaretRight /></el-icon>
              执行
            </el-button>
            <el-button @click="clearSql">
              <el-icon><Delete /></el-icon>
              清空
            </el-button>
            <el-button @click="showHistoryDrawer = true">
              <el-icon><Clock /></el-icon>
              历史记录
              <el-badge v-if="sqlHistory.length > 0" :value="sqlHistory.length" :max="99" style="margin-left: 6px;" />
            </el-button>
          </div>
        </div>
      </template>
      
      <el-alert
        title="仅支持SELECT查询语句"
        type="warning"
        description="为了安全考虑，SQL执行器只允许执行查询语句，不支持INSERT、UPDATE、DELETE等修改操作"
        :closable="false"
        show-icon
        style="margin-bottom: 15px;">
      </el-alert>
      
      <el-input
        v-model="sqlText"
        type="textarea"
        :rows="8"
        placeholder="请输入SELECT查询语句..."
        style="margin-bottom: 15px; font-family: 'Courier New', monospace;"
        resize="none">
      </el-input>
      
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <el-tag v-if="sqlResult" type="success" size="small">
            <el-icon><SuccessFilled /></el-icon>
            <span style="font-weight: 600;">查询成功: {{ sqlResult.rowCount }} 行数据</span>
          </el-tag>
          <el-tag v-if="sqlResult && sqlResult.hasMore" type="warning" size="small" style="margin-left: 8px;">
            <el-icon><Warning /></el-icon>
            数据已截断(最多显示1000行)
          </el-tag>
        </div>
        <el-button-group>
          <el-button size="small" @click="formatSql">
            <el-icon><MagicStick /></el-icon>
            格式化
          </el-button>
        </el-button-group>
      </div>
    </el-card>
    
    <!-- 查询结果 -->
    <el-card v-if="sqlResult && sqlResult.success" shadow="hover">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon><Document /></el-icon>
            <span style="font-weight: 600;">查询结果</span>
            <el-tag type="success" size="small">{{ sqlResult.rowCount }} 行</el-tag>
            <el-tag v-if="sqlResult.columns" type="info" size="small">{{ sqlResult.columns.length }} 列</el-tag>
          </div>
          <el-button type="primary" size="small" @click="exportSqlResult">
            <el-icon><Download /></el-icon>
            导出结果
          </el-button>
        </div>
      </template>
      
      <el-table
        :data="sqlResult.data"
        stripe
        border
        style="width: 100%"
        height="500"
        v-loading="executingSql">
        <el-table-column
          v-for="column in sqlResult.columns"
          :key="column.name"
          :prop="column.name"
          :label="column.name"
          width="200"
          show-overflow-tooltip>
          <template #header>
            <div style="display: flex; flex-direction: column; gap: 4px;">
              <span style="font-weight: 600; font-size: 12px;">{{ column.name }}</span>
              <el-tag size="small" type="info" style="font-size: 10px;">{{ column.type }}</el-tag>
            </div>
          </template>
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 6px;">
              <span 
                style="font-family: 'Courier New', monospace; font-size: 12px; flex: 1;"
                :class="{'null-value': scope.row[column.name] === null}">
                {{ formatCellValue(scope.row[column.name]) }}
              </span>
              <el-button 
                v-if="shouldShowViewButton(scope.row[column.name])"
                type="primary" 
                size="small" 
                text
                @click="viewCellData(column.name, scope.row[column.name], column.type)">
                <el-icon size="14"><View /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 错误信息 -->
    <el-card v-if="sqlResult && !sqlResult.success" shadow="hover" style="border-left: 4px solid #f56c6c;">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
          <span style="font-weight: 600; color: #f56c6c;">执行错误</span>
        </div>
      </template>
      <div style="color: #f56c6c; font-family: 'Courier New', monospace; padding: 10px; background: #fef0f0; border-radius: 4px;">
        {{ sqlResult.message }}
      </div>
    </el-card>

    <!-- 单元格数据查看对话框 -->
    <el-dialog
      v-model="cellDataDialog.visible"
      :title="cellDataDialog.title"
      width="600px">
      <el-alert 
        :title="`数据类型: ${cellDataDialog.type}`" 
        type="info" 
        :closable="false"
        style="margin-bottom: 15px;">
      </el-alert>
      <el-input
        v-model="cellDataDialog.content"
        type="textarea"
        :rows="15"
        readonly
        style="font-family: 'Courier New', monospace;">
      </el-input>
      <template #footer>
        <div style="display: flex; justify-content: space-between;">
          <el-tag size="small">长度: {{ cellDataDialog.content?.length || 0 }} 字符</el-tag>
          <div>
            <el-button @click="copyCellData">
              <el-icon><DocumentCopy /></el-icon>
              复制
            </el-button>
            <el-button type="primary" @click="cellDataDialog.visible = false">关闭</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 执行历史抽屉 -->
    <el-drawer
      v-model="showHistoryDrawer"
      title="SQL 执行历史"
      direction="rtl"
      size="500px">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon><Clock /></el-icon>
            <span style="font-weight: 600;">SQL 执行历史</span>
          </div>
          <el-button type="danger" size="small" text @click="clearHistory" :disabled="sqlHistory.length === 0">
            <el-icon><Delete /></el-icon>
            清空历史
          </el-button>
        </div>
      </template>
      
      <el-alert
        title="历史记录存储在本地浏览器中，最多保留100条记录"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 15px;">
      </el-alert>
      
      <div v-if="sqlHistory.length === 0" style="text-align: center; padding: 40px; color: #909399;">
        <el-icon size="48"><Document /></el-icon>
        <p>暂无执行历史</p>
      </div>
      
      <div v-else class="history-list">
        <div
          v-for="(item, index) in sqlHistory"
          :key="item.id"
          class="history-item"
          @click="loadHistorySql(item)">
          <div class="history-header">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-tag :type="item.success ? 'success' : 'danger'" size="small">
                {{ item.success ? '成功' : '失败' }}
              </el-tag>
              <el-tag type="info" size="small">{{ item.dataSource }}</el-tag>
            </div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span class="history-time">{{ formatTime(item.timestamp) }}</span>
              <el-button
                type="danger"
                size="small"
                text
                @click.stop="deleteHistoryItem(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="history-sql">{{ truncateSql(item.sql) }}</div>
          <div v-if="item.success && item.rowCount !== undefined" class="history-result">
            返回 {{ item.rowCount }} 行数据
          </div>
          <div v-if="!item.success && item.errorMessage" class="history-error">
            {{ truncateError(item.errorMessage) }}
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>

import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useDatasourceStore } from '@/stores/datasource'
import { executeSql } from '@/api/sql'

const datasourceStore = useDatasourceStore()

const sqlDataSource = ref('')
const sqlText = ref('')
const sqlResult = ref(null)
const executingSql = ref(false)

const cellDataDialog = reactive({
  visible: false,
  title: '',
  content: '',
  type: ''
})

// 执行历史相关
const HISTORY_STORAGE_KEY = 'sql_executor_history'
const MAX_HISTORY_COUNT = 100
const showHistoryDrawer = ref(false)
const sqlHistory = ref([])

// 加载历史记录
const loadHistory = () => {
  try {
    const stored = localStorage.getItem(HISTORY_STORAGE_KEY)
    if (stored) {
      sqlHistory.value = JSON.parse(stored)
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    sqlHistory.value = []
  }
}

// 保存历史记录
const saveHistory = () => {
  try {
    localStorage.setItem(HISTORY_STORAGE_KEY, JSON.stringify(sqlHistory.value))
  } catch (error) {
    console.error('保存历史记录失败:', error)
  }
}

// 添加历史记录
const addHistoryItem = (sql, dataSource, success, rowCount, errorMessage) => {
  // 检查是否存在相同SQL和数据源的记录，如果存在则移除旧记录
  const existingIndex = sqlHistory.value.findIndex(
    item => item.sql.trim() === sql.trim() && item.dataSource === dataSource
  )
  if (existingIndex !== -1) {
    sqlHistory.value.splice(existingIndex, 1)
  }
  
  const item = {
    id: Date.now(),
    sql,
    dataSource,
    success,
    rowCount,
    errorMessage,
    timestamp: new Date().toISOString()
  }
  
  // 添加到开头
  sqlHistory.value.unshift(item)
  
  // 超过100条则删除最旧的
  if (sqlHistory.value.length > MAX_HISTORY_COUNT) {
    sqlHistory.value = sqlHistory.value.slice(0, MAX_HISTORY_COUNT)
  }
  
  saveHistory()
}

// 删除单条历史记录
const deleteHistoryItem = (index) => {
  sqlHistory.value.splice(index, 1)
  saveHistory()
  ElMessage.success('已删除该条记录')
}

// 清空历史记录
const clearHistory = () => {
  sqlHistory.value = []
  saveHistory()
  ElMessage.success('历史记录已清空')
}

// 加载历史SQL到编辑器
const loadHistorySql = (item) => {
  sqlText.value = item.sql
  sqlDataSource.value = item.dataSource
  showHistoryDrawer.value = false
  ElMessage.success('已加载历史SQL')
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 截断SQL显示
const truncateSql = (sql) => {
  const trimmed = sql.trim().replace(/\s+/g, ' ')
  return trimmed.length > 100 ? trimmed.substring(0, 100) + '...' : trimmed
}

// 截断错误信息
const truncateError = (msg) => {
  return msg.length > 80 ? msg.substring(0, 80) + '...' : msg
}

const executeSqlQuery = async () => {
  if (!sqlDataSource.value || !sqlText.value.trim()) {
    ElMessage.warning('请选择数据源并输入SQL语句')
    return
  }
  
  executingSql.value = true
  try {
    const res = await executeSql({
      dataSourceName: sqlDataSource.value,
      sql: sqlText.value.trim(),
      params: []
    })
    
    sqlResult.value = {
      success: true,
      columns: res.data.columns,
      data: res.data.data,
      rowCount: res.data.rowCount,
      hasMore: res.data.hasMore
    }
    // 添加成功记录到历史
    addHistoryItem(sqlText.value.trim(), sqlDataSource.value, true, res.data.rowCount, null)
    ElMessage.success('SQL执行成功')
  } catch (error) {
    sqlResult.value = {
      success: false,
      message: error.message
    }
    // 添加失败记录到历史
    addHistoryItem(sqlText.value.trim(), sqlDataSource.value, false, null, error.message)
    ElMessage.error('SQL执行失败')
  } finally {
    executingSql.value = false
  }
}

const clearSql = () => {
  sqlText.value = ''
  sqlResult.value = null
  ElMessage.info('已清空SQL')
}

const formatSql = () => {
  if (!sqlText.value.trim()) {
    ElMessage.warning('请先输入SQL语句')
    return
  }
  
  try {
    let sql = sqlText.value.trim()
    sql = sql.replace(/\b(SELECT|FROM|WHERE|ORDER BY|GROUP BY|HAVING|JOIN|LEFT JOIN|RIGHT JOIN|INNER JOIN)\b/gi, '\n$1')
    sql = sql.replace(/\n\s*\n/g, '\n')
    
    let lines = sql.split('\n')
    let indentLevel = 0
    lines = lines.map(line => {
      line = line.trim()
      if (!line) return ''
      
      if (line.startsWith('FROM') || line.startsWith('WHERE') ||
          line.startsWith('ORDER BY') || line.startsWith('GROUP BY') ||
          line.startsWith('HAVING')) {
        indentLevel = Math.max(0, indentLevel - 1)
      }
      
      const indentedLine = '  '.repeat(indentLevel) + line
      
      if (line.startsWith('SELECT') || line.startsWith('FROM') ||
          line.startsWith('WHERE') || line.includes('JOIN')) {
        indentLevel++
      }
      
      return indentedLine
    })
    
    sqlText.value = lines.filter(line => line !== '').join('\n')
    ElMessage.success('SQL格式化完成')
  } catch (error) {
    ElMessage.warning('格式化失败，请检查SQL语法')
  }
}

const exportSqlResult = () => {
  if (!sqlResult.value || !sqlResult.value.success) {
    ElMessage.warning('没有可导出的查询结果')
    return
  }
  
  try {
    let csvContent = ''
    const headers = sqlResult.value.columns.map(col => col.name)
    csvContent += headers.join(',') + '\n'
    
    sqlResult.value.data.forEach(row => {
      const values = headers.map(header => {
        const value = row[header]
        if (value === null || value === undefined) return 'NULL'
        if (typeof value === 'string' && (value.includes(',') || value.includes('\n') || value.includes('"'))) {
          return '"' + value.replace(/"/g, '""') + '"'
        }
        return String(value)
      })
      csvContent += values.join(',') + '\n'
    })
    
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.href = url
    link.download = `query_result_${new Date().getTime()}.csv`
    link.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败: ' + error.message)
  }
}

const formatCellValue = (value) => {
  if (value === null || value === undefined) return 'NULL'
  const strValue = String(value)
  if (strValue.length > 50) return strValue.substring(0, 50) + '...'
  return strValue
}

const shouldShowViewButton = (value) => {
  if (value === null || value === undefined) return false
  const strValue = String(value)
  return strValue.length > 50 || strValue.includes('\n')
}

const viewCellData = (columnName, value, type) => {
  cellDataDialog.title = `查看字段: ${columnName}`
  cellDataDialog.content = value === null || value === undefined ? 'NULL' : String(value)
  cellDataDialog.type = type
  cellDataDialog.visible = true
}

const copyCellData = () => {
  navigator.clipboard.writeText(cellDataDialog.content).then(() => {
    ElMessage.success('数据已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

onMounted(async () => {
  if (datasourceStore.dataSources.length === 0) {
    await datasourceStore.loadDataSources()
  }
  // 加载历史记录
  loadHistory()
})
</script>

<style scoped>
.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}

.null-value {
  color: #909399;
  font-style: italic;
  opacity: 0.7;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.history-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-time {
  font-size: 12px;
  color: #909399;
}

.history-sql {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #303133;
  background-color: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  word-break: break-all;
}

.history-result {
  margin-top: 8px;
  font-size: 12px;
  color: #67c23a;
}

.history-error {
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
}
</style>
