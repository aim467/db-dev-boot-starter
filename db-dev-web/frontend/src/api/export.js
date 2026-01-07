import request from './request'

/**
 * 导出表结构文档
 * @param {string} dataSourceName - 数据源名称
 * @param {string} format - 导出格式: markdown, html
 * @returns {Promise} 返回包含 data 和 headers 的对象
 */
export const exportSchema = (dataSourceName, format) => {
  // 直接使用 axios，不经过 request 拦截器
  return request({
    url: `/export/${format}`,
    method: 'get',
    params: { dataSourceName },
    responseType: 'blob'
  })
}