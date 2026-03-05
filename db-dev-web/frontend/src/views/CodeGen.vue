<template>
  <div>
    <el-card shadow="hover" class="config-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Tools /></el-icon>
            <span class="header-title">代码生成器</span>
          </div>
          <el-tag type="success" size="small">v2.0</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" label-width="110px" :rules="rules">
        <div class="section-title">
          <el-icon><Setting /></el-icon>
          <span>基础配置</span>
        </div>

        <el-row :gutter="20">
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
                placeholder="请选择数据表"
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
            <el-form-item label="生成框架" prop="generationType">
              <el-select v-model="form.generationType" style="width: 100%" @change="onFrameworkChange">
                <el-option label="MyBatis" value="MYBATIS" />
                <el-option label="MyBatis-Plus" value="MYBATIS_PLUS" />
                <el-option label="JPA" value="JPA" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="生成组件" prop="components">
              <el-select
                v-model="form.components"
                multiple
                collapse-tags
                collapse-tags-tooltip
                style="width: 100%"
                placeholder="默认全部"
              >
                <el-option
                  v-for="item in availableComponents"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="基础包名" prop="basePackage">
              <el-input v-model="form.basePackage" placeholder="com.example.project" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="作者名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-collapse v-model="activeCollapse" class="advanced-config">
          <el-collapse-item name="advanced">
            <template #title>
              <div class="collapse-title">
                <el-icon><Operation /></el-icon>
                <span>高级配置</span>
              </div>
            </template>

            <el-row :gutter="20" style="margin-top: 14px">
              <el-col :span="24">
                <el-form-item label="通用">
                  <el-checkbox v-model="form.overwrite">覆盖已存在文件</el-checkbox>
                  <el-checkbox v-model="form.entity.useLombok">使用 Lombok</el-checkbox>
                  <el-checkbox v-model="form.entity.useSwagger">使用 Swagger 注解</el-checkbox>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 10px">
              <el-col :span="8">
                <el-form-item label="Entity 父类">
                  <el-input v-model="form.entity.superClass" placeholder="可选，如 BaseEntity" />
                </el-form-item>
              </el-col>

              <el-col :span="8" v-if="form.generationType !== 'JPA' && form.components.includes('MAPPER')">
                <el-form-item label="Mapper 后缀">
                  <el-input v-model="form.mapper.suffix" placeholder="Mapper" />
                </el-form-item>
              </el-col>

              <el-col :span="8" v-if="form.generationType !== 'JPA' && form.components.includes('XML')">
                <el-form-item label="XML 目录">
                  <el-input v-model="form.xml.directory" placeholder="mapper" />
                </el-form-item>
              </el-col>

              <el-col :span="8" v-if="form.generationType === 'JPA' && form.components.includes('REPOSITORY')">
                <el-form-item label="Repository 后缀">
                  <el-input v-model="form.repository.suffix" placeholder="Repository" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 10px" v-if="form.generationType !== 'JPA' && form.components.includes('SERVICE')">
              <el-col :span="8">
                <el-form-item label="Service 接口后缀">
                  <el-input v-model="form.service.interfaceSuffix" placeholder="Service" />
                </el-form-item>
              </el-col>

              <el-col :span="8">
                <el-form-item label="Service 实现后缀">
                  <el-input v-model="form.service.implSuffix" placeholder="ServiceImpl" />
                </el-form-item>
              </el-col>

              <el-col :span="8">
                <el-form-item label="Service 生成策略">
                  <el-radio-group v-model="form.service.generateInterface">
                    <el-radio :value="true">接口 + 实现</el-radio>
                    <el-radio :value="false">仅实现类</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 10px" v-if="form.components.includes('CONTROLLER')">
              <el-col :span="8">
                <el-form-item label="Controller 后缀">
                  <el-input v-model="form.controller.suffix" placeholder="Controller" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="请求前缀">
                  <el-input v-model="form.controller.basePath" placeholder="默认按表名推导" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>

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
        <el-table-column prop="columnName" label="字段名" width="160" />
        <el-table-column prop="dataType" label="类型" width="120" />
        <el-table-column prop="javaType" label="Java类型" width="140" />
        <el-table-column prop="nullable" label="可空" width="80" align="center" />
        <el-table-column prop="primaryKey" label="主键" width="80" align="center" />
        <el-table-column prop="remarks" label="备注" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-card v-if="hasPreviewCode" shadow="hover" class="preview-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Document /></el-icon>
            <span class="header-title">代码预览</span>
          </div>
          <el-button size="small" @click="copyAllCode">
            <el-icon><DocumentCopy /></el-icon>
            复制全部
          </el-button>
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
              <el-button size="small" text @click="copyCode(code)">复制</el-button>
            </div>
            <div class="code-content">
              <pre><code>{{ code }}</code></pre>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDatasourceList } from '@/api/datasource'
import { getTableDetail, getTableList } from '@/api/metadata'
import { generate, preview } from '@/api/codegen'

const formRef = ref()
const dataSources = ref([])
const tables = ref([])
const tableColumns = ref([])
const previewCode = ref({})
const previewLoading = ref(false)
const generateLoading = ref(false)
const activeCollapse = ref([])
const activeTab = ref('')

const frameworkComponents = {
  MYBATIS: [
    { label: 'Entity', value: 'ENTITY' },
    { label: 'Mapper', value: 'MAPPER' },
    { label: 'XML', value: 'XML' },
    { label: 'Service', value: 'SERVICE' },
    { label: 'Controller', value: 'CONTROLLER' }
  ],
  MYBATIS_PLUS: [
    { label: 'Entity', value: 'ENTITY' },
    { label: 'Mapper', value: 'MAPPER' },
    { label: 'XML', value: 'XML' },
    { label: 'Service', value: 'SERVICE' },
    { label: 'Controller', value: 'CONTROLLER' }
  ],
  JPA: [
    { label: 'Entity', value: 'ENTITY' },
    { label: 'Repository', value: 'REPOSITORY' },
    { label: 'Controller', value: 'CONTROLLER' }
  ]
}

const defaultComponents = (framework) => frameworkComponents[framework]?.map(item => item.value) || []

const form = reactive({
  dataSourceName: '',
  tableName: '',
  basePackage: 'com.example.project',
  author: 'DB Dev',
  generationType: 'MYBATIS',
  components: defaultComponents('MYBATIS'),
  overwrite: true,
  entity: {
    useLombok: true,
    useSwagger: false,
    useJpa: false,
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
    suffix: 'Repository'
  },
  service: {
    interfaceSuffix: 'Service',
    implSuffix: 'ServiceImpl',
    generateInterface: true
  },
  controller: {
    suffix: 'Controller',
    basePath: ''
  }
})

const rules = {
  dataSourceName: [{ required: true, message: '请选择数据源', trigger: 'change' }],
  tableName: [{ required: true, message: '请选择数据表', trigger: 'change' }],
  generationType: [{ required: true, message: '请选择生成框架', trigger: 'change' }],
  components: [{ required: true, message: '请至少选择一个生成组件', trigger: 'change' }],
  basePackage: [{ required: true, message: '请输入基础包名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }]
}

const availableComponents = computed(() => frameworkComponents[form.generationType] || [])
const hasPreviewCode = computed(() => Object.keys(previewCode.value).length > 0)

const getDbType = (type) => {
  const types = { mysql: 'success', postgresql: 'primary', oracle: 'warning', sqlserver: '' }
  return types[type?.toLowerCase()] || 'info'
}

const getTabLabel = (type) => {
  const labels = {
    entity: 'Entity',
    mapper: 'Mapper',
    xml: 'XML',
    repository: 'Repository',
    service: 'Service',
    serviceimpl: 'ServiceImpl',
    controller: 'Controller'
  }
  return labels[type?.toLowerCase()] || type
}

const getFileExtension = (type) => type?.toLowerCase() === 'xml' ? '.xml' : '.java'

const onFrameworkChange = (framework) => {
  const allowed = new Set(defaultComponents(framework))
  form.components = form.components.filter(item => allowed.has(item))
  if (form.components.length === 0) {
    form.components = defaultComponents(framework)
  }
}

watch(
  () => form.generationType,
  (framework) => {
    form.entity.useJpa = framework === 'JPA'
    if (framework === 'JPA') {
      form.service.generateInterface = true
    }
  },
  { immediate: true }
)

const loadDataSources = async () => {
  try {
    const res = await getDatasourceList()
    dataSources.value = res.data || []
  } catch (error) {
    console.error('加载数据源失败', error)
  }
}

const onDataSourceChange = async (val) => {
  form.tableName = ''
  tableColumns.value = []
  previewCode.value = {}
  if (!val) {
    tables.value = []
    return
  }
  try {
    const res = await getTableList(val)
    tables.value = res.data || []
  } catch (error) {
    console.error('加载表列表失败', error)
    tables.value = []
  }
}

const onTableChange = async (val) => {
  previewCode.value = {}
  if (!val || !form.dataSourceName) {
    tableColumns.value = []
    return
  }
  try {
    const res = await getTableDetail(val, form.dataSourceName)
    tableColumns.value = res.data?.columns || []
  } catch (error) {
    console.error('加载表结构失败', error)
    tableColumns.value = []
  }
}

const buildRequestConfig = () => ({ ...form, components: form.components })

const handlePreview = async () => {
  try {
    await formRef.value.validate()
    previewLoading.value = true

    const config = { ...buildRequestConfig(), outputDir: null }
    const res = await preview(config)
    if (res.code === 200) {
      previewCode.value = res.data || {}
      activeTab.value = Object.keys(previewCode.value)[0] || ''
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

const handleGenerate = async () => {
  try {
    await formRef.value.validate()
    generateLoading.value = true

    const res = await generate(buildRequestConfig())
    if (res.code === 200) {
      previewCode.value = res.data?.files || {}
      activeTab.value = Object.keys(previewCode.value)[0] || ''
      ElMessage.success('代码生成成功，开始下载')

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

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const copyAllCode = async () => {
  const allCode = Object.entries(previewCode.value)
    .map(([type, code]) => `// ===== ${type.toUpperCase()} =====\n${code}`)
    .join('\n\n')
  await copyCode(allCode)
}

const resetForm = () => {
  formRef.value?.resetFields()
  form.generationType = 'MYBATIS'
  form.components = defaultComponents('MYBATIS')
  form.overwrite = true
  form.entity.useLombok = true
  form.entity.useSwagger = false
  form.entity.superClass = ''
  form.mapper.suffix = 'Mapper'
  form.xml.directory = 'mapper'
  form.repository.suffix = 'Repository'
  form.service.interfaceSuffix = 'Service'
  form.service.implSuffix = 'ServiceImpl'
  form.service.generateInterface = true
  form.controller.suffix = 'Controller'
  form.controller.basePath = ''

  previewCode.value = {}
  tableColumns.value = []
}

loadDataSources()
</script>

<style scoped>
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

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
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
</style>
