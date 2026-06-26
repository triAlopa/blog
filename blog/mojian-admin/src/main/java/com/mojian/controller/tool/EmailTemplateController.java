package com.mojian.controller.tool;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mojian.common.Result;
import com.mojian.entity.SysEmailTemplate;
import com.mojian.service.EmailTemplateService;
import com.mojian.utils.PageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邮件模板控制器
 */
@RestController
@RequestMapping("/tool/email-template")
@RequiredArgsConstructor
@Api(tags = "邮件模板管理")
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @GetMapping("/list")
    @ApiOperation(value = "获取邮件模板列表")
    public Result<IPage<SysEmailTemplate>> list(SysEmailTemplate query) {
        return Result.success(emailTemplateService.selectPage(query));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取邮件模板详情")
    public Result<SysEmailTemplate> getInfo(@PathVariable Long id) {
        return Result.success(emailTemplateService.getById(id));
    }

    @GetMapping("/code/{templateCode}")
    @ApiOperation(value = "根据模板编码获取模板")
    public Result<SysEmailTemplate> getByCode(@PathVariable String templateCode) {
        return Result.success(emailTemplateService.getByCode(templateCode));
    }

    @PostMapping
    @SaCheckPermission("tool:emailTemplate:add")
    @ApiOperation(value = "添加邮件模板")
    public Result<Void> add(@RequestBody SysEmailTemplate template) {
        emailTemplateService.save(template);
        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("tool:emailTemplate:edit")
    @ApiOperation(value = "修改邮件模板")
    public Result<Void> edit(@RequestBody SysEmailTemplate template) {
        emailTemplateService.updateById(template);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("tool:emailTemplate:remove")
    @ApiOperation(value = "删除邮件模板")
    public Result<Void> remove(@PathVariable List<Long> ids) {
        emailTemplateService.removeByIds(ids);
        return Result.success();
    }
}
