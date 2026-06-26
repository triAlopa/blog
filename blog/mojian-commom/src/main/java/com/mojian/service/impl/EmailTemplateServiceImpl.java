package com.mojian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mojian.entity.SysEmailTemplate;
import com.mojian.mapper.SysEmailTemplateMapper;
import com.mojian.service.EmailTemplateService;
import com.mojian.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 邮件模板服务实现类
 * 优先从数据库读取模板，如果数据库没有则从文件读取
 */
@Slf4j
@Service
public class EmailTemplateServiceImpl extends ServiceImpl<SysEmailTemplateMapper, SysEmailTemplate> implements EmailTemplateService {

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    @Override
    public IPage<SysEmailTemplate> selectPage(SysEmailTemplate query) {
        LambdaQueryWrapper<SysEmailTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(query.getTemplateName() != null, SysEmailTemplate::getTemplateName, query.getTemplateName());
        wrapper.like(query.getTemplateCode() != null, SysEmailTemplate::getTemplateCode, query.getTemplateCode());
        wrapper.orderByDesc(SysEmailTemplate::getCreateTime);
        return page(PageUtil.getPage(), wrapper);
    }

    @Override
    public SysEmailTemplate getByCode(String templateCode) {
        return getOne(new LambdaQueryWrapper<SysEmailTemplate>()
                .eq(SysEmailTemplate::getTemplateCode, templateCode)
                .eq(SysEmailTemplate::getStatus, 1));
    }

    @Override
    public String renderTemplate(String templateCode, Map<String, String> variables) {
        // 1. 优先从数据库获取模板
        SysEmailTemplate template = getByCode(templateCode);

        String content;
        if (template != null && template.getContent() != null && !template.getContent().isEmpty()) {
            // 数据库有模板，使用数据库模板
            content = template.getContent();
            log.debug("使用数据库邮件模板: {}", templateCode);
        } else {
            // 2. 数据库没有模板，从文件读取
            content = loadTemplateFromFile(templateCode);
            log.debug("使用文件邮件模板: {}", templateCode);
        }

        if (content == null) {
            log.warn("邮件模板不存在: {}", templateCode);
            return null;
        }

        // 3. 使用 Thymeleaf 渲染模板
        return renderContent(content, variables);
    }

    @Override
    public String renderContent(String content, Map<String, String> variables) {
        if (content == null || variables == null) {
            return content;
        }

        // 如果有 Thymeleaf 引擎，使用 Thymeleaf 渲染
        if (templateEngine != null) {
            try {
                Context context = new Context();
                java.util.Map<String, Object> contextVars = new java.util.HashMap<>(variables);
                context.setVariables(contextVars);
                return templateEngine.process(content, context);
            } catch (Exception e) {
                log.warn("Thymeleaf 渲染失败，使用简单替换: {}", e.getMessage());
            }
        }

        // 降级方案：简单字符串替换
        String result = content;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(key, value);
        }
        return result;
    }

    /**
     * 从文件加载模板
     *
     * @param templateCode 模板编码
     * @return 模板内容
     */
    private String loadTemplateFromFile(String templateCode) {
        String filePath = "templates/email/" + templateCode + ".html";
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            if (resource.exists()) {
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("读取模板文件失败: {}", filePath, e);
        }
        return null;
    }
}
