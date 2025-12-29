import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory('/db-dev/'),
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
