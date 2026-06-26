package com.mojian.service;

import java.util.Map;

/**
 * Bangumi 追番服务接口
 */
public interface BangumiService {

    /**
     * 获取用户收藏列表
     * @param type 收藏类型：1=想看, 2=看过, 3=在看, 4=搁置, 5=抛弃
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 收藏列表数据
     */
    Object getCollections(int type, int offset, int limit);

    /**
     * 获取所有追番数据（在看+想看+看过+搁置+抛弃）
     * @return 所有追番数据
     */
    Map<String, Object> getAllCollections();

    /**
     * 获取条目详情
     * @param subjectId 条目ID
     * @return 条目详情
     */
    Object getSubject(int subjectId);

    /**
     * 获取条目相关人员信息
     * @param subjectId 条目ID
     * @return 相关人员信息
     */
    Object getSubjectPersons(int subjectId);

    /**
     * 获取用户信息
     * @return 用户信息
     */
    Object getUserInfo();

    /**
     * 测试 API 连接
     * @return 测试结果
     */
    String testConnection();
}
