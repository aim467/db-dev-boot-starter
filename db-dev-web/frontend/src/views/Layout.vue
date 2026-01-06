<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="240px">
      <div class="sidebar-header">
        <h1>
          <el-icon><Database /></el-icon>
          DB Dev
        </h1>
        <p>Database Development Assistant</p>
      </div>
      <el-menu
        :default-active="currentRoute"
        background-color="transparent"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router>
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header height="60px">
        <h2>{{ currentViewTitle }}</h2>
        <div style="margin-left: auto; display: flex; align-items: center; gap: 10px;">
          <span style="color: #909399; font-size: 14px; margin-right: 8px;">
            {{ username }}
          </span>
          <el-tooltip content="刷新页面" placement="bottom">
            <el-button circle size="small" @click="refreshCurrentView">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="登出" placement="bottom">
            <el-button circle size="small" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </el-header>
      <el-main class="content-body">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const username = ref('')

const menuItems = [
  { path: '/dashboard', label: 'Dashboard', icon: 'HomeFilled' },
  { path: '/datasources', label: '数据源', icon: 'Connection' },
  { path: '/tables', label: '表结构', icon: 'Folder' },
  { path: '/sql', label: 'SQL 执行器', icon: 'Lightning' },
  { path: '/codegen', label: '代码生成', icon: 'Tools' },
  { path: '/druid', label: 'Druid 监控', icon: 'DataAnalysis' }
]

const currentRoute = computed(() => route.path)

const currentViewTitle = computed(() => {
  const item = menuItems.find(m => m.path === route.path)
  return item ? item.label : ''
})

const refreshCurrentView = () => {
  router.go(0)
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用登出接口
    try {
      await request.post('/auth/logout')
    } catch (error) {
      // 忽略登出接口错误
    }
    
    // 清除本地存储
    localStorage.removeItem('db-dev-token')
    localStorage.removeItem('db-dev-username')
    
    ElMessage.success('已退出登录')
    
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  username.value = localStorage.getItem('db-dev-username') || '用户'
})
</script>

<style scoped>
.app-container {
  height: 100%;
}

.el-aside {
  background: linear-gradient(180deg, #1a1f2e 0%, #0f1419 100%);
  color: #fff;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
}

.el-aside::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 50%, rgba(64, 158, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.sidebar-header {
  padding: 24px 20px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(103, 194, 58, 0.05) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  z-index: 1;
}

.sidebar-header h1 {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: 0.5px;
}

.sidebar-header h1 .el-icon {
  font-size: 28px;
  color: #409eff;
  filter: drop-shadow(0 0 8px rgba(64, 158, 255, 0.5));
}

.sidebar-header p {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 6px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.el-menu {
  border-right: none;
  background-color: transparent;
  padding: 12px 0;
}

:deep(.el-menu-item) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 6px 12px;
  border-radius: 10px;
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

:deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #409eff 0%, #67c23a 100%);
  border-radius: 0 3px 3px 0;
  transition: height 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.15) 0%, rgba(64, 158, 255, 0.05) 100%) !important;
  transform: translateX(4px);
}

:deep(.el-menu-item:hover::before) {
  height: 24px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.25) 0%, rgba(64, 158, 255, 0.1) 100%) !important;
  color: #409eff !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

:deep(.el-menu-item.is-active::before) {
  height: 32px;
}

:deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 12px;
}

.el-header {
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  padding: 0 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 10;
}

.el-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-body {
  padding: 24px;
  height: calc(100vh - 60px);
  overflow-y: auto;
  background: #f5f7fa;
  position: relative;
}

.content-body::before {
  content: '';
  position: fixed;
  top: 0;
  left: 240px;
  right: 0;
  height: 300px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.03) 0%, rgba(103, 194, 58, 0.02) 100%);
  pointer-events: none;
  z-index: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
