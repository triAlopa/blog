import request from '@/utils/request'

// ==================== 敏感词管理 ====================

// 获取敏感词列表
export function getSensitiveWordListApi() {
  return request({
    url: '/system/sensitive-word/list',
    method: 'get'
  })
}

// 新增敏感词
export function addSensitiveWordApi(data: any) {
  return request({
    url: '/system/sensitive-word',
    method: 'post',
    data
  })
}

// 批量新增敏感词
export function batchAddSensitiveWordApi(data: any[]) {
  return request({
    url: '/system/sensitive-word/batch',
    method: 'post',
    data
  })
}

// 删除敏感词
export function deleteSensitiveWordApi(id: number) {
  return request({
    url: `/system/sensitive-word/${id}`,
    method: 'delete'
  })
}

// 批量删除敏感词
export function batchDeleteSensitiveWordApi(ids: number[]) {
  return request({
    url: '/system/sensitive-word/batch',
    method: 'delete',
    data: ids
  })
}

// ==================== 白名单管理 ====================

// 获取白名单列表
export function getWhitelistApi() {
  return request({
    url: '/system/sensitive-word/whitelist',
    method: 'get'
  })
}

// 新增白名单
export function addWhitelistApi(data: any) {
  return request({
    url: '/system/sensitive-word/whitelist',
    method: 'post',
    data
  })
}

// 批量新增白名单
export function batchAddWhitelistApi(data: any[]) {
  return request({
    url: '/system/sensitive-word/whitelist/batch',
    method: 'post',
    data
  })
}

// 删除白名单
export function deleteWhitelistApi(id: number) {
  return request({
    url: `/system/sensitive-word/whitelist/${id}`,
    method: 'delete'
  })
}

// ==================== 词库管理 ====================

// 刷新词库
export function reloadSensitiveWordApi() {
  return request({
    url: '/system/sensitive-word/reload',
    method: 'post'
  })
}

// 测试敏感词检测
export function testSensitiveWordApi(text: string) {
  return request({
    url: '/system/sensitive-word/test',
    method: 'get',
    params: { text }
  })
}
