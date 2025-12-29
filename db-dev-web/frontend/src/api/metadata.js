import request from './request'

export const getTableList = (dataSourceName) => {
  return request({
    url: '/metadata/tables',
    method: 'get',
    params: { dataSourceName }
  })
}

export const getTableDetail = (tableName, dataSourceName) => {
  return request({
    url: `/metadata/table/${tableName}`,
    method: 'get',
    params: { dataSourceName }
  })
}
