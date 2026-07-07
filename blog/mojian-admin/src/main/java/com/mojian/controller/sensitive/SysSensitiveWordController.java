package com.mojian.controller.sensitive;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mojian.annotation.OperationLogger;
import com.mojian.common.Result;
import com.mojian.idempotent.annotation.RepeatSubmit;
import com.mojian.sensitive.domain.SysSensitiveWord;
import com.mojian.sensitive.domain.SysSensitiveWordWhitelist;
import com.mojian.service.ISysSensitiveWordService;
import com.mojian.sensitive.service.SensitiveWordBsHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 敏感词管理 Controller
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sensitive-word")
@Api(tags = "敏感词管理")
public class SysSensitiveWordController {

    private final ISysSensitiveWordService sensitiveWordService;
    private final SensitiveWordBsHolder sensitiveWordBsHolder;

    // ==================== 敏感词管理 ====================

    /**
     * 查询敏感词列表
     */
    @ApiOperation("查询敏感词列表")
    @GetMapping("/list")
    @OperationLogger("敏感词")
    @SaCheckPermission("system:sensitive:list")
    public Result<List<SysSensitiveWord>> list() {
        return Result.success(sensitiveWordService.selectList());
    }

    /**
     * 新增敏感词
     */
    @RepeatSubmit
    @ApiOperation("新增敏感词")
    @PostMapping
    @OperationLogger("敏感词")
    @SaCheckPermission("system:sensitive:add")
    public Result<Boolean> add(@RequestBody SysSensitiveWord word) {
        return Result.success(sensitiveWordService.insert(word) > 0);
    }

    /**
     * 批量新增敏感词
     */
    @RepeatSubmit
    @ApiOperation("批量新增敏感词")
    @PostMapping("/batch")
    @OperationLogger("敏感词")
    @SaCheckPermission("system:sensitive:add")
    public Result<Boolean> batchAdd(@RequestBody List<SysSensitiveWord> words) {
        return Result.success(sensitiveWordService.batchInsert(words) > 0);
    }

    /**
     * 删除敏感词
     */
    @RepeatSubmit
    @ApiOperation("删除敏感词")
    @DeleteMapping("/{id}")
    @OperationLogger("敏感词")
    @SaCheckPermission("system:sensitive:delete")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sensitiveWordService.deleteById(id) > 0);
    }

    /**
     * 批量删除敏感词
     */
    @RepeatSubmit
    @ApiOperation("批量删除敏感词")
    @DeleteMapping("/batch")
    @OperationLogger("敏感词")
    @SaCheckPermission("system:sensitive:delete")
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        return Result.success(sensitiveWordService.deleteByIds(ids) > 0);
    }

    // ==================== 白名单管理 ====================

    /**
     * 查询白名单列表
     */
    @ApiOperation("查询白名单列表")
    @GetMapping("/whitelist")
    @OperationLogger("敏感词")
    public Result<List<SysSensitiveWordWhitelist>> whitelist() {
        return Result.success(sensitiveWordService.selectWhitelist());
    }

    /**
     * 新增白名单
     */
    @RepeatSubmit
    @ApiOperation("新增白名单")
    @PostMapping("/whitelist")
    @OperationLogger("敏感词")
    public Result<Boolean> addWhitelist(@RequestBody SysSensitiveWordWhitelist whitelist) {
        return Result.success(sensitiveWordService.insertWhitelist(whitelist) > 0);
    }

    /**
     * 批量新增白名单
     */
    @RepeatSubmit
    @ApiOperation("批量新增白名单")
    @PostMapping("/whitelist/batch")
    @OperationLogger("敏感词")
    public Result<Boolean> batchAddWhitelist(@RequestBody List<SysSensitiveWordWhitelist> whitelists) {
        return Result.success(sensitiveWordService.batchInsertWhitelist(whitelists) > 0);
    }

    /**
     * 删除白名单
     */
    @RepeatSubmit
    @ApiOperation("删除白名单")
    @DeleteMapping("/whitelist/{id}")
    @OperationLogger("敏感词")
    public Result<Boolean> deleteWhitelist(@PathVariable Long id) {
        return Result.success(sensitiveWordService.deleteWhitelistById(id) > 0);
    }

    // ==================== 词库管理 ====================

    /**
     * 手动刷新词库
     */
    @RepeatSubmit
    @ApiOperation("手动刷新词库")
    @PostMapping("/reload")
    @OperationLogger("敏感词")
    @SaCheckPermission("system:sensitive:reload")
    public Result<Void> reload() {
        sensitiveWordService.reload();
        return Result.success();
    }

    /**
     * 测试敏感词检测
     */
    @ApiOperation("测试敏感词检测")
    @GetMapping("/test")
    @OperationLogger("敏感词")
    @RepeatSubmit
    public Result<Boolean> test(@RequestParam String text) {
        return Result.success(sensitiveWordBsHolder.contains(text));
    }
}
