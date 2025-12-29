<template>
  <div class="codegen-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon>
            <Tools />
          </el-icon>
          <span class="header-title">代码生成</span>
        </div>
      </template>

      <!-- 配置表单 -->
      <el-form ref="formRef" :model="form" label-width="120px" :rules="rules">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数据源" prop="dataSourceName">
              <el-select v-model="form.dataSourceName" placeholder="请选择数据源" style="width: 100%;">
                <el-option v-for="ds in dataSources" :key="ds.name" :label="ds.name" :value="ds.name">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="表名" prop="tableName">
              <el-select v-model="form.tableName" placeholder="请选择表" style="width: 100%;"
                :disabled="!form.dataSourceName">
                <el-option v-for="table in tables" :key="table.tableName" :label="table.tableName"
                  :value="table.tableName">
                  <span>{{ table.tableName }}</span>
                  <el-tag v-if="table.remarks" size="small" type="info">{{ table.remarks }}</el-tag>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="基础包名" prop="basePackage">
              <el-input v-model="form.basePackage" placeholder="如: com.example.project" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="作者名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生成类型" prop="generateType">
              <el-select v-model="form.generateType" placeholder="请选择生成类型" style="width: 100%;">
                <el-option label="全部" value="ALL" />
                <el-option label="Entity" value="ENTITY" />
                <el-option label="Mapper" value="MAPPER" />
                <el-option label="XML" value="XML" />
                <el-option label="Repository" value="REPOSITORY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="输出目录" prop="outputDir">
              <el-input v-model="form.outputDir" placeholder="留空则只预览不保存">
                <template #append>
                  <el-button @click="selectOutputDir">选择</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label=" ">
          <el-checkbox v-model="form.overwrite">覆盖已存在文件</el-checkbox>
          <el-checkbox v-model="form.entity.useLombok">使用 Lombok</el-checkbox>
          <el-checkbox v-model="form.entity.useJpa">使用 JPA 注解</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handlePreview" :loading="previewLoading">
            <el-icon>
              <View />
            </el-icon>
            预览代码
          </el-button>
          <el-button type="success" @click="handleGenerate" :loading="generateLoading" :disabled="!form.outputDir">
            <el-icon>
              <Download />
            </el-icon>
            生成代码
          </el-button>
          <el-button @click="resetForm">
            <el-icon>
              <Refresh />
            </el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 代码预览 -->
    <el-card v-if="previewCode && Object.keys(previewCode).length > 0" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <el-icon>
            <Document />
          </el-icon>
          <span class="header-title">代码预览</span>
        </div>
      </template>

      <el-tabs type="border-card">
        <el-tab-pane v-for="(code, type) in previewCode" :key="type" :label="type">
          <div class="code-container">
            <el-button size="small" @click="copyCode(code)"
              style="position: absolute; right: 10px; top: 10px; z-index: 10;">
              <el-icon>
                <DocumentCopy />
              </el-icon>
              复制
            </el-button>
            <pre><code>{{ code }}</code></pre>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDatasourceList, testConnection, deleteDatasource } from '@/api/datasource'
import { getTableList, getTableDetail } from '@/api/metadata'
import { generate, preview } from '@/api/codegen'

const formRef = ref()
const dataSources = ref([])
const tables = ref([])
const previewCode = ref({})
const previewLoading = ref(false)
const generateLoading = ref(false)

const form = reactive({
  dataSourceName: '',
  tableName: '',
  basePackage: 'com.example.project',
  author: 'DB Dev',
  generateType: 'ALL',
  outputDir: '',
  overwrite: false,
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
  dataSourceName: [
    { required: true, message: '请选择数据源', trigger: 'change' }
  ],
  tableName: [
    { required: true, message: '请选择表', trigger: 'change' }
  ],
  basePackage: [
    { required: true, message: '请输入基础包名', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者名称', trigger: 'blur' }
  ]
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

// 监听数据源变化，加载表列表
watch(() => form.dataSourceName, async (newVal) => {
  if (newVal) {
    try {
      const res = await getTableList(newVal)
      tables.value = res.data || []
      form.tableName = ''
    } catch (error) {
      console.error('加载表列表失败:', error)
      tables.value = []
    }
  }
})

// 预览代码
const handlePreview = async () => {
  try {
    await formRef.value.validate()
    previewLoading.value = true

    const config = { ...form }
    config.outputDir = null // 预览模式不输出文件

    const res = await preview(config)
    if (res.code === 200) {
      previewCode.value = res.data
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

// 生成代码
const handleGenerate = async () => {
  try {
    await formRef.value.validate()
    if (!form.outputDir) {
      ElMessage.warning('请选择输出目录')
      return
    }

    generateLoading.value = true

    const res = await generate(form)
    if (res.code === 200) {
      ElMessage.success('代码生成成功')
      previewCode.value = res.data
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
const copyCode = (code) => {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('代码已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 选择输出目录
const selectOutputDir = () => {
  ElMessage.info('请手动输入输出目录路径')
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  previewCode.value = {}
  form.entity.useLombok = true
  form.entity.useJpa = false
  form.overwrite = false
}

// 初始化
loadDataSources()
</script>

<style scoped>
.codegen-container {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-weight: 600;
}

.code-container {
  position: relative;
  background: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
}

.code-container pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #24292e;
}

.code-container code {
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
