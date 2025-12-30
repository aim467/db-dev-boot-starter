import request from './request'

export const getDatasourceList = () => {
  return request({
    url: '/datasource/list',
    method: 'get'
  })
}

export const testConnection = (name) => {
  return request({
    url: '/datasource/test',
    method: 'post',
    data: { name }
  })
}

export const deleteDatasource = (name) => {
  return request({
    url: `/datasource/${name}`,
    method: 'delete'
  })
}

export const getPoolStats = (dataSourceName) => {
  return request({
    url: '/datasource/pool-stats',
    method: 'get',
    params: { dataSourceName }
  })
}
