<template>
  <el-card shadow="hover">
    <template #header>
      <div style="display: flex; align-items: center; justify-content: space-between;">
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon><Document /></el-icon>
          <span style="font-weight: 600;">表详情: {{ table.tableName }}</span>
          <el-tag v-if="table.tableType" type="info" size="small">{{ table.tableType }}</el-tag>
        </div>
        <div style="display: flex; gap: 8px;">
          <el-button type="primary" size="small" @click="exportTableInfo">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          <el-button size="small" @click="$emit('close')">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
      </div>
    </template>

    <el-tabs type="border-card">
      <el-tab-pane>
        <template #label>
          <span style="display: flex; align-items: center; gap: 4px;">
            <el-icon><List /></el-icon>
            字段信息 ({{ table.columns?.length || 0 }})
          </span>
        </template>
        <el-table :data="table.columns" stripe border style="width: 100%" height="400">
          <el-table-column prop="columnName" label="字段名" width="180" fixed>
            <template #default="scope">
              <div style="display: flex; align-items: center; gap: 6px;">
                <el-icon v-if="scope.row.primaryKey" color="#f56c6c" size="14"><Key /></el-icon>
                <strong>{{ scope.row.columnName }}</strong>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="typeName" label="类型" width="120" sortable />
          <el-table-column prop="columnSize" label="大小" width="80" sortable>
            <template #default="scope">
              {{ scope.row.columnSize > 0 ? scope.row.columnSize : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="可空" width="80" sortable>
            <template #default="scope">
              <el-tag :type="scope.row.nullable ? 'info' : 'warning'" size="small">
                {{ scope.row.nullable ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="columnDef" label="默认值" width="120">
            <template #default="scope">
              {{ scope.row.defaultValue || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="remarks" label="备注" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.remarks || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="自增" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.autoIncrement" type="success" size="small">是</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane>
        <template #label>
          <span style="display: flex; align-items: center; gap: 4px;">
            <el-icon><Key /></el-icon>
            索引信息 ({{ groupedIndexes.length }})
          </span>
        </template>
        <el-empty v-if="groupedIndexes.length === 0" description="无索引信息" />
        <el-table v-else :data="groupedIndexes" stripe border style="width: 100%">
          <el-table-column prop="indexName" label="索引名" width="200" />
          <el-table-column label="包含列" min-width="250">
            <template #default="scope">
              <div style="display: flex; flex-wrap: wrap; gap: 4px;">
                <el-tag 
                  v-for="(col, idx) in scope.row.columns" 
                  :key="idx" 
                  size="small"
                  type="info"
                >
                  {{ col.columnName }}
                  <span v-if="col.ascOrDesc" style="color: #909399; margin-left: 2px;">
                    ({{ col.ascOrDesc }})
                  </span>
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="唯一" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.unique ? 'success' : 'info'" size="small">
                {{ scope.row.unique ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="150">
            <template #default="scope">
              {{ scope.row.type || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane>
        <template #label>
          <span style="display: flex; align-items: center; gap: 4px;">
            <el-icon><DocumentCopy /></el-icon>
            SQL预览
          </span>
        </template>
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span>CREATE TABLE 语句</span>
              <el-button type="primary" size="small" @click="copySql">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
            </div>
          </template>
          <el-input
            type="textarea"
            :rows="15"
            :model-value="generateCreateTableSql"
            readonly
            style="font-family: 'Courier New', monospace;"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  table: {
    type: Object,
    required: true
  }
})

defineEmits(['close'])

// 将相同索引名的记录合并
const groupedIndexes = computed(() => {
  if (!props.table?.indexes || props.table.indexes.length === 0) return []
  
  const indexMap = new Map()
  props.table.indexes.forEach(idx => {
    const key = idx.indexName
    if (!indexMap.has(key)) {
      indexMap.set(key, {
        indexName: idx.indexName,
        unique: idx.unique,
        type: idx.type,
        columns: []
      })
    }
    indexMap.get(key).columns.push({
      columnName: idx.columnName,
      ascOrDesc: idx.ascOrDesc || 'ASC'
    })
  })
  
  return Array.from(indexMap.values())
})

const generateCreateTableSql = computed(() => {
  if (!props.table || !props.table.columns) return ''
  
  const tableName = props.table.tableName
  const parts = []
  
  // 字段定义
  const columns = props.table.columns.map(col => {
    let sql = `  \`${col.columnName}\` ${col.typeName}`
    if (col.columnSize > 0) {
      sql += `(${col.columnSize})`
    }
    
    if (!col.nullable) {
      sql += ' NOT NULL'
    }

    if (col.autoIncrement) {
      sql += ' AUTO_INCREMENT'
    }
    if (col.columnDef) {
      sql += ` DEFAULT '${col.columnDef}'`
    }
    if (col.remarks) {
      sql += ` COMMENT '${col.remarks}'`
    }
    return sql
  })
  parts.push(...columns)
  
  // 主键
  const primaryKeys = props.table.columns.filter(col => col.primaryKey)
  if (primaryKeys.length > 0) {
    const pkCols = primaryKeys.map(col => `\`${col.columnName}\``).join(', ')
    parts.push(`  PRIMARY KEY (${pkCols})`)
  }
  
  // 索引
  if (groupedIndexes.value.length > 0) {
    groupedIndexes.value.forEach(idx => {
      // 跳过主键索引
      if (idx.indexName === 'PRIMARY') return
      
      const idxCols = idx.columns.map(col => `\`${col.columnName}\``).join(', ')
      if (idx.unique) {
        parts.push(`  UNIQUE KEY \`${idx.indexName}\` (${idxCols})`)
      } else {
        parts.push(`  KEY \`${idx.indexName}\` (${idxCols})`)  
      }
    })
  }
  
  return `CREATE TABLE \`${tableName}\` (\n${parts.join(',\n')}\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='${props.table.remarks || ''}';`
})

const exportTableInfo = () => {
  const data = JSON.stringify(props.table, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${props.table.tableName}.json`
  link.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('导出成功')
}

const copySql = () => {
  navigator.clipboard.writeText(generateCreateTableSql.value).then(() => {
    ElMessage.success('SQL已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}
</script>

<style scoped>
.el-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
}
</style>
