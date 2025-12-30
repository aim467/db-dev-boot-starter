import axios from 'axios'
import { ElMessage } from 'element-plus'

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
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request
