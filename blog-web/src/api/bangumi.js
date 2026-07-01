import request from '@/utils/request'

/**
 * 获取追番列表
 */
export function getCollectionsApi(type = 3, offset = 0, limit = 50) {
  return request({
    url: '/bangumi/collections',
    method: 'get',
    params: { type, offset, limit }
  })
}

/**
 * 获取所有追番数据
 */
export function getAllCollectionsApi() {
  return request({
    url: '/bangumi/all',
    method: 'get'
  })
}

/**
 * 获取所有漫画数据
 */
export function getAllMangaCollectionsApi() {
  return request({
    url: '/bangumi/manga',
    method: 'get'
  })
}

/**
 * 获取条目详情
 */
export function getSubjectApi(subjectId) {
  return request({
    url: `/bangumi/subject/${subjectId}`,
    method: 'get'
  })
}

/**
 * 获取条目相关人员信息（制作方等）
 */
export function getSubjectPersonsApi(subjectId) {
  return request({
    url: `/bangumi/subject/${subjectId}/persons`,
    method: 'get'
  })
}

/**
 * 获取用户信息
 */
export function getUserInfoApi() {
  return request({
    url: '/bangumi/user',
    method: 'get'
  })
}

/**
 * 测试 API 连接
 */
export function testConnectionApi() {
  return request({
    url: '/bangumi/test',
    method: 'get'
  })
}
