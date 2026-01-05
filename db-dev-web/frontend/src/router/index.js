import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'

// 从当前 URL 动态获取 basePath，适配任何 context-path + uiPath 配置
const getBasePath = () => {
  // 通过 assets 目录位置确定 UI 根路径
  const scriptEl = document.querySelector('script[src*="assets/"]')
  if (scriptEl?.src) {
    const url = new URL(scriptEl.src)
    const assetsIndex = url.pathname.indexOf('/assets/')
    if (assetsIndex > 0) {
      return url.pathname.substring(0, assetsIndex) + '/'
    }
  }
  // fallback
  const pathname = window.location.pathname
  if (pathname.endsWith('.html')) {
    return pathname.substring(0, pathname.lastIndexOf('/') + 1)
  }
  const pathParts = pathname.split('/').filter(Boolean)
  if (pathParts.length > 0) {
    return '/' + pathParts[0] + '/'
  }
  return '/db-dev/'
}

// 获取API基础URL
const getBaseURL = () => {
  if (window.__DB_DEV_CONFIG__?.apiBaseURL) {
    return window.__DB_DEV_CONFIG__.apiBaseURL
  }
  let pathname = window.location.pathname
  if (pathname.endsWith('.html')) {
    pathname = pathname.substring(0, pathname.lastIndexOf('/'))
  }
  const basePath = document.querySelector('script[src*="assets/"]')?.src
  if (basePath) {
    const url = new URL(basePath)
    const assetsIndex = url.pathname.indexOf('/assets/')
    if (assetsIndex > 0) {
      return url.pathname.substring(0, assetsIndex) + '/api'
    }
  }
  const pathParts = pathname.split('/').filter(Boolean)
  if (pathParts.length >= 2) {
    return '/' + pathParts.slice(0, -1).join('/') + '/api'
  }
  if (pathParts.length === 1) {
    return '/' + pathParts[0] + '/api'
  }
  return '/db-dev/api'
}

// 检查安全认证状态（缓存结果）
let securityEnabled = null
let securityCheckPromise = null

const checkSecurityStatus = async () => {
  if (securityEnabled !== null) {
    return securityEnabled
  }
  
  if (securityCheckPromise) {
    return securityCheckPromise
  }
  
  securityCheckPromise = axios.get(`${getBaseURL()}/auth/security-status`)
    .then(response => {
      securityEnabled = response.data?.data?.enabled ?? true
      return securityEnabled
    })
    .catch(() => {
      // 如果请求失败，默认启用安全认证
      securityEnabled = true
      return securityEnabled
    })
    .finally(() => {
      securityCheckPromise = null
    })
  
  return securityCheckPromise
}

const router = createRouter({
  history: createWebHistory(getBasePath()),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/views/Layout.vue'),
      redirect: '/dashboard',
      meta: { requiresAuth: true },
      children: [
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue')
        },
        {
          path: '/datasources',
          name: 'DataSources',
          component: () => import('@/views/DataSources.vue')
        },
        {
          path: '/tables',
          name: 'Tables',
          component: () => import('@/views/Tables.vue')
        },
        {
          path: '/sql',
          name: 'SqlExecutor',
          component: () => import('@/views/SqlExecutor.vue')
        },
        {
          path: '/codegen',
          name: 'CodeGen',
          component: () => import('@/views/CodeGen.vue')
        }
      ]
    },
    // 处理未知路由，跳转到404页面
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/404.vue')
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  // 如果路由不需要认证，直接通过
  if (!requiresAuth) {
    next()
    return
  }
  
  // 检查安全认证是否启用
  const isSecurityEnabled = await checkSecurityStatus()
  
  // 如果安全认证未启用，直接通过
  if (!isSecurityEnabled) {
    next()
    return
  }
  
  // 安全认证已启用，检查token（优先使用 localStorage，其次使用 sessionStorage）
  const token = localStorage.getItem('db-dev-token') || sessionStorage.getItem('db-dev-token')
  
  // 如果路由需要认证但没有token，跳转到登录页
  if (!token) {
    next('/login')
    return
  }
  
  // 如果有token且访问登录页，跳转到首页
  if (to.path === '/login') {
    next('/dashboard')
    return
  }
  
  // 其他情况正常导航
  next()
})

// 导出检查安全状态的函数，供其他模块使用
export { checkSecurityStatus }

export default router
