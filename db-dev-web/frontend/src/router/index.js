import { createRouter, createWebHistory } from 'vue-router'

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

const router = createRouter({
  history: createWebHistory(getBasePath()),
  routes: [
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/views/Layout.vue'),
      redirect: '/dashboard',
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
    // 处理 /index.html 等路径，重定向到首页
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

export default router
