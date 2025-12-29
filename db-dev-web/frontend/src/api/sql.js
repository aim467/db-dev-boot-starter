import request from './request'

export const executeSql = (data) => {
  return request({
    url: '/sql/execute',
    method: 'post',
    data
  })
}
