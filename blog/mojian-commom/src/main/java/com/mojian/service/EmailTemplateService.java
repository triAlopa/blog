package com.mojian.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mojian.entity.SysEmailTemplate;

import java.util.Map;

/**
 * 邮件模板服务接口
 */
public interface EmailTemplateService extends IService<SysEmailTemplate> {

    /**
     * 分页查询邮件模板
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<SysEmailTemplate> selectPage(SysEmailTemplate query);

    /**
     * 根据模板编码获取模板
     *
     * @param templateCode 模板编码
     * @return 模板对象
     */
    SysEmailTemplate getByCode(String templateCode);

    /**
     * 渲染模板（替换变量）
     *
     * @param templateCode 模板编码
     * @param variables    变量（键值对）
     * @return 渲染后的HTML内容
     */
    String renderTemplate(String templateCode, Map<String, String> variables);

    /**
     * 渲染模板内容
     *
     * @param content   模板内容
     * @param variables 变量
     * @return 渲染后的HTML内容
     */
    String renderContent(String content, Map<String, String> variables);
}
