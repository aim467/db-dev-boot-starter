import request from './request'

/**
 * 检查 Druid 是否启用
 */
export function checkDruidEnabled() {
  return request.get('/druid/enabled')
}

/**
 * 获取连接池状态
 */
export function getPoolStats() {
  return request.get('/druid/pool-stats')
}

/**
 * 获取 SQL 统计信息
 */
export function getSqlStats() {
  return request.get('/druid/sql-stats')
}

export function getUrlStats() {
  return request.get('/druid/url-stats')
}

/**
 * 获取会话统计信息
 */
export function getSessionStats() {
  return request.get('/druid/session-stats')
}

/**
 * 重置统计信息
 */
export function resetStats() {
  return request.post('/druid/reset-stats')
}
