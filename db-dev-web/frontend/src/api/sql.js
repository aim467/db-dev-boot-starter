import request from './request'

export const executeSql = (data) => {
  return request({
    url: '/sql/execute',
    method: 'post',
    data
  })
}

export const analyzeSql = (data) => {
  return request({
    url: '/sql/analyze',
    method: 'post',
    data
  })
}

// AI 分析相关 API
export const getAiStatus = () => {
  return request({
    url: '/ai/status',
    method: 'get'
  })
}

export const analyzeSqlWithAi = (data) => {
  return request({
    url: '/ai/analyze/sql',
    method: 'post',
    data
  })
}

export const analyzeExplainWithAi = (data) => {
  return request({
    url: '/ai/analyze/explain',
    method: 'post',
    data
  })
}

export const analyzeTablesWithAi = (data) => {
  return request({
    url: '/ai/analyze/tables',
    method: 'post',
    data
  })
}
