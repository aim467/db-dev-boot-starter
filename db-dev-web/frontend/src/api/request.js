import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 支持通过环境变量或运行时配置自定义 API 地址
// 默认从当前 URL 动态推断，自动适配任何 context-path + uiPath 配置
const getBaseURL = () => {
  // 优先使用运行时配置（可通过 window.__DB_DEV_CONFIG__ 注入）
  if (window.__DB_DEV_CONFIG__?.apiBaseURL) {
    return window.__DB_DEV_CONFIG__.apiBaseURL
  }
  // 从当前路径动态获取 basePath
  // 例如 /app/db/index.html -> /app/db/api
  // 例如 /db-dev/dashboard -> /db-dev/api
  let pathname = window.location.pathname
  // 移除末尾的文件名（如 index.html）
  if (pathname.endsWith('.html')) {
    pathname = pathname.substring(0, pathname.lastIndexOf('/'))
  }
  // 移除末尾的子路由路径，保留到 UI 根路径
  // 通过查找 assets 目录的相对位置来确定根路径
  const basePath = document.querySelector('script[src*="assets/"]')?.src
  if (basePath) {
    const url = new URL(basePath)
    const assetsIndex = url.pathname.indexOf('/assets/')
    if (assetsIndex > 0) {
      return url.pathname.substring(0, assetsIndex) + '/api'
    }
  }
  // fallback: 取当前路径去掉最后一级子路由
  const pathParts = pathname.split('/').filter(Boolean)
  if (pathParts.length >= 2) {
    // 假设最后一级是子路由（dashboard/tables 等），取前面的作为 basePath
    // 但如果只有一级，就用这一级
    return '/' + pathParts.slice(0, -1).join('/') + '/api'
  }
  if (pathParts.length === 1) {
    return '/' + pathParts[0] + '/api'
  }
  return '/db-dev/api'
}

const request = axios.create({
  baseURL: getBaseURL(),
  timeout: 30000
})

  // 请求拦截器
request.interceptors.request.use(
  config => {
    // 添加token到请求头，优先使用 localStorage，其次使用 sessionStorage
    const token = localStorage.getItem('db-dev-token') || sessionStorage.getItem('db-dev-token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 对于 blob 响应，直接返回原始 response（包含 headers）
    const contentType = response.headers?.['content-type'] || ''
    if (response.data instanceof Blob || contentType.includes('application/octet-stream')) {
      return response
    }
    
    const res = response.data
    
    // 如果返回的状态码不是200，则认为是错误
    if (res.code !== 200) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    
    return res
  },
  error => {
    console.error('Response error:', error)
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      // 清除本地存储的token
      localStorage.removeItem('db-dev-token')
      localStorage.removeItem('db-dev-username')
      
      ElMessage.error('登录已过期，请重新登录')
      
      // 跳转到登录页
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
      
      return Promise.reject(error)
    }
    
    // 其他错误
    const message = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
