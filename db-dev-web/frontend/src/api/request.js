import axios from 'axios'
import { ElMessage } from 'element-plus'

// 支持通过环境变量或运行时配置自定义 API 地址
// 默认使用相对路径，自动适配部署域名
const getBaseURL = () => {
  // 优先使用运行时配置（可通过 window.__DB_DEV_CONFIG__ 注入）
  if (window.__DB_DEV_CONFIG__?.apiBaseURL) {
    return window.__DB_DEV_CONFIG__.apiBaseURL
  }
  // 使用相对路径，适配任何部署环境
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
