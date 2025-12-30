<template>
  <div class="codegen-container">
    <!-- 配置卡片 -->
    <el-card shadow="hover" class="config-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Tools /></el-icon>
            <span class="header-title">代码生成器</span>
          </div>
          <el-tag type="success" size="small">v1.0</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" label-width="100px" :rules="rules">
        <!-- 基础配置 -->
        <div class="section-title">
          <el-icon><Setting /></el-icon>
          <span>基础配置</span>
        </div>
        
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="数据源" prop="dataSourceName">
              <el-select 
                v-model="form.dataSourceName" 
                placeholder="请选择数据源" 
                style="width: 100%"
                @change="onDataSourceChange"
              >
                <el-option 
                  v-for="ds in dataSources" 
                  :key="ds.name" 
                  :label="ds.name" 
                  :value="ds.name"
                >
                  <div class="ds-option">
                    <span>{{ ds.name }}</span>
                    <el-tag size="small" :type="getDbType(ds.type)">{{ ds.type }}</el-tag>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数据表" prop="tableName">
              <el-select 
                v-model="form.tableName" 
                placeholder="请选择表" 
                style="width: 100%"
                :disabled="!form.dataSourceName"
                filterable
                @change="onTableChange"
              >
                <el-option 
                  v-for="table in tables" 
                  :key="table.tableName" 
                  :label="table.tableName" 
                  :value="table.tableName"
                >
                  <div class="table-option">
                    <span>{{ table.tableName }}</span>
                    <span v-if="table.remarks" class="table-remarks">{{ table.remarks }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="生成类型" prop="generateTypes">
              <el-select v-model="form.generateTypes" multiple collapse-tags collapse-tags-tooltip style="width: 100%" placeholder="默认全部">
                <el-option label="Entity" value="ENTITY" />
                <el-option label="Mapper" value="MAPPER" />
                <el-option label="XML" value="XML" />
                <el-option label="Repository" value="REPOSITORY" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="基础包名" prop="basePackage">
              <el-input v-model="form.basePackage" placeholder="com.example.project" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="作者名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 高级配置 -->
        <el-collapse v-model="activeCollapse" class="advanced-config">
          <el-collapse-item name="advanced">
            <template #title>
              <div class="collapse-title">
                <el-icon><Operation /></el-icon>
                <span>高级配置</span>
              </div>
            </template>
            
            <el-row :gutter="24">
              <el-col :span="24">
                <div class="checkbox-group">
                  <el-checkbox v-model="form.overwrite">覆盖已存在文件</el-checkbox>
                  <el-checkbox v-model="form.entity.useLombok">使用 Lombok</el-checkbox>
                  <el-checkbox v-model="form.entity.useJpa">使用 JPA 注解</el-checkbox>
                  <el-checkbox v-model="form.entity.useSwagger">使用 Swagger 注解</el-checkbox>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="24" style="margin-top: 16px">
              <el-col :span="8">
                <el-form-item label="Entity父类">
                  <el-input v-model="form.entity.superClass" placeholder="可选，如 BaseEntity" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="Mapper后缀">
                  <el-input v-model="form.mapper.suffix" placeholder="Mapper" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="XML目录">
                  <el-input v-model="form.xml.directory" placeholder="mapper" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button @click="resetForm">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="primary" @click="handlePreview" :loading="previewLoading">
            <el-icon><View /></el-icon>
            预览代码
          </el-button>
          <el-button type="success" @click="handleGenerate" :loading="generateLoading">
            <el-icon><Download /></el-icon>
            生成并下载
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 表结构预览 -->
    <el-card v-if="tableColumns.length > 0" shadow="hover" class="table-preview-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Grid /></el-icon>
            <span class="header-title">表结构预览</span>
            <el-tag size="small" type="info">{{ form.tableName }}</el-tag>
          </div>
          <span class="column-count">共 {{ tableColumns.length }} 个字段</span>
        </div>
      </template>
      
      <el-table :data="tableColumns" stripe size="small" max-height="300">
        <el-table-column prop="columnName" label="字段名" width="150" />
        <el-table-column prop="dataType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.dataType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="javaType" label="Java类型" width="120" />
        <el-table-column prop="nullable" label="可空" width="80" align="center">
          <template #default="{ row }">
            <el-icon :color="row.nullable ? '#67c23a' : '#f56c6c'">
              <component :is="row.nullable ? 'Check' : 'Close'" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="primaryKey" label="主键" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.primaryKey" size="small" type="warning">PK</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" show-overflow-tooltip />
      </el-table>
    </el-card>

    <!-- 代码预览 -->
    <el-card v-if="hasPreviewCode" shadow="hover" class="preview-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Document /></el-icon>
            <span class="header-title">代码预览</span>
          </div>
          <el-button-group size="small">
            <el-button @click="copyAllCode">
              <el-icon><DocumentCopy /></el-icon>
              复制全部
            </el-button>
          </el-button-group>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card" class="code-tabs">
        <el-tab-pane 
          v-for="(code, type) in previewCode" 
          :key="type" 
          :label="getTabLabel(type)" 
          :name="type"
        >
          <div class="code-wrapper">
            <div class="code-toolbar">
              <span class="file-type">{{ getFileExtension(type) }}</span>
              <el-button size="small" text @click="copyCode(code)">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
            </div>
            <div class="code-content">
              <pre><code :class="getLanguageClass(type)">{{ code }}</code></pre>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getDatasourceList } from '@/api/datasource'
import { getTableList, getTableDetail } from '@/api/metadata'
import { generate, preview, getDownloadUrl } from '@/api/codegen'

const formRef = ref()
const dataSources = ref([])
const tables = ref([])
const tableColumns = ref([])
const previewCode = ref({})
const previewLoading = ref(false)
const generateLoading = ref(false)
const activeCollapse = ref([])
const activeTab = ref('')
const downloadUrl = ref('')

const form = reactive({
  dataSourceName: '',
  tableName: '',
  basePackage: 'com.example.project',
  author: 'DB Dev',
  generateTypes: [],
  overwrite: true,
  entity: {
    useLombok: true,
    useJpa: false,
    useSwagger: false,
    superClass: ''
  },
  mapper: {
    suffix: 'Mapper',
    superClass: ''
  },
  xml: {
    directory: 'mapper'
  },
  repository: {
    suffix: 'Repository',
    useJpa: true
  }
})

const rules = {
  dataSourceName: [{ required: true, message: '请选择数据源', trigger: 'change' }],
  tableName: [{ required: true, message: '请选择表', trigger: 'change' }],
  basePackage: [{ required: true, message: '请输入基础包名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者名称', trigger: 'blur' }]
}

const hasPreviewCode = computed(() => Object.keys(previewCode.value).length > 0)

// 获取数据库类型标签样式
const getDbType = (type) => {
  const types = { mysql: 'success', postgresql: 'primary', oracle: 'warning', sqlserver: '' }
  return types[type?.toLowerCase()] || 'info'
}

// 获取Tab标签
const getTabLabel = (type) => {
  const labels = {
    entity: 'Entity',
    mapper: 'Mapper',
    xml: 'XML',
    repository: 'Repository',
    service: 'Service',
    controller: 'Controller'
  }
  return labels[type?.toLowerCase()] || type
}

// 获取文件扩展名
const getFileExtension = (type) => {
  const ext = type?.toLowerCase()
  if (ext === 'xml') return '.xml'
  return '.java'
}

// 获取语法高亮类
const getLanguageClass = (type) => {
  return type?.toLowerCase() === 'xml' ? 'language-xml' : 'language-java'
}

// 加载数据源列表
const loadDataSources = async () => {
  try {
    const res = await getDatasourceList()
    dataSources.value = res.data || []
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

// 数据源变化
const onDataSourceChange = async (val) => {
  form.tableName = ''
  tableColumns.value = []
  previewCode.value = {}
  if (val) {
    try {
      const res = await getTableList(val)
      tables.value = res.data || []
    } catch (error) {
      console.error('加载表列表失败:', error)
      tables.value = []
    }
  }
}

// 表变化，加载表结构
const onTableChange = async (val) => {
  previewCode.value = {}
  if (val && form.dataSourceName) {
    try {
      const res = await getTableDetail(val, form.dataSourceName)
      tableColumns.value = res.data?.columns || []
    } catch (error) {
      console.error('加载表结构失败:', error)
      tableColumns.value = []
    }
  }
}

// 预览代码
const handlePreview = async () => {
  try {
    await formRef.value.validate()
    previewLoading.value = true

    const config = { ...form, outputDir: null }
    const res = await preview(config)
    
    if (res.code === 200) {
      previewCode.value = res.data
      activeTab.value = Object.keys(res.data)[0] || ''
      ElMessage.success('代码预览成功')
    } else {
      ElMessage.error(res.message || '预览失败')
    }
  } catch (error) {
    console.error('预览失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '预览失败')
  } finally {
    previewLoading.value = false
  }
}

// 生成代码并下载
const handleGenerate = async () => {
  try {
    await formRef.value.validate()

    generateLoading.value = true
    const res = await generate(form)
    
    if (res.code === 200) {
      ElMessage.success('代码生成成功，正在下载...')
      previewCode.value = res.data.files
      activeTab.value = Object.keys(res.data.files)[0] || ''
      downloadUrl.value = res.data.downloadUrl
      
      // 自动触发下载
      const link = document.createElement('a')
      link.href = res.data.downloadUrl
      link.download = res.data.fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    console.error('生成失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '生成失败')
  } finally {
    generateLoading.value = false
  }
}

// 复制代码
const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

// 复制全部代码
const copyAllCode = async () => {
  const allCode = Object.entries(previewCode.value)
    .map(([type, code]) => `// ========== ${type.toUpperCase()} ==========\n${code}`)
    .join('\n\n')
  await copyCode(allCode)
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  previewCode.value = {}
  tableColumns.value = []
  downloadUrl.value = ''
  form.entity.useLombok = true
  form.entity.useJpa = false
  form.entity.useSwagger = false
  form.overwrite = false
}

// 初始化
loadDataSources()
</script>

<style scoped>
.codegen-container {
  padding: 10px;
  /* 圆角，阴影 */
  /* max-width: 1400px; */
  margin: 0 auto;
}

.config-card {
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.config-card,
.table-preview-card,
.preview-card {
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-weight: 600;
  font-size: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  font-size: 14px;
}

.ds-option,
.table-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.table-remarks {
  color: #909399;
  font-size: 12px;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.input-icon {
  cursor: pointer;
  color: #909399;
}

.input-icon:hover {
  color: #409eff;
}

.advanced-config {
  margin: 20px 0;
  border: none;
}

.advanced-config :deep(.el-collapse-item__header) {
  background: #f5f7fa;
  padding: 0 16px;
  border-radius: 4px;
}

.collapse-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.checkbox-group {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.column-count {
  color: #909399;
  font-size: 13px;
}

.code-tabs {
  border: none;
}

.code-tabs :deep(.el-tabs__header) {
  background: #f5f7fa;
  margin: 0;
}

.code-tabs :deep(.el-tabs__content) {
  padding: 0;
}

.code-wrapper {
  background: #1e1e1e;
  border-radius: 0 0 4px 4px;
  overflow: hidden;
}

.code-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #2d2d2d;
  border-bottom: 1px solid #3d3d3d;
}

.file-type {
  color: #858585;
  font-size: 12px;
  font-family: monospace;
}

.code-toolbar .el-button {
  color: #cccccc;
}

.code-toolbar .el-button:hover {
  color: #ffffff;
}

.code-content {
  max-height: 500px;
  overflow: auto;
  padding: 16px;
}

.code-content pre {
  margin: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.code-content code {
  color: #d4d4d4;
  white-space: pre;
}

/* 简单的语法高亮 */
.code-content code.language-java {
  color: #9cdcfe;
}

.code-content code.language-xml {
  color: #ce9178;
}

/* 滚动条样式 */
.code-content::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.code-content::-webkit-scrollbar-track {
  background: #1e1e1e;
}

.code-content::-webkit-scrollbar-thumb {
  background: #424242;
  border-radius: 4px;
}

.code-content::-webkit-scrollbar-thumb:hover {
  background: #4f4f4f;
}
</style>
