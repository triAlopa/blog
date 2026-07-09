package com.mojian.controller.site;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mojian.annotation.OperationLogger;
import com.mojian.idempotent.annotation.RepeatSubmit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import com.mojian.entity.SysFriend;
import com.mojian.service.SysFriendService;
import com.mojian.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

/**
 * 友情链接 控制器
 */
@RestController
@Api(tags = "友情链接管理")
@RequestMapping("/sys/friend")
@RequiredArgsConstructor
public class SysFriendController {

    private final SysFriendService sysFriendService;

    @GetMapping("/list")
    @ApiOperation(value = "友情链接列表")
    @OperationLogger(value = "友情链接",module = "管理端")
    public Result<IPage<SysFriend>> list(SysFriend sysFriend) {
        return Result.success(sysFriendService.selectPage(sysFriend));
    }

    @PostMapping("add")
    @ApiOperation(value = "新增友情链接")
    @SaCheckPermission("sys:friend:add")
    @OperationLogger(value = "友情链接",module = "管理端")
    @RepeatSubmit
    public Result<Object> add(@RequestBody SysFriend sysFriend) {
        return Result.success(sysFriendService.save(sysFriend));
    }

    @PutMapping("update")
    @ApiOperation(value = "修改友情链接")
    @SaCheckPermission("sys:friend:update")
    @OperationLogger(value = "友情链接",module = "管理端")
    @RepeatSubmit
    public Result<Object> update(@RequestBody SysFriend sysFriend) {
        return Result.success(sysFriendService.update(sysFriend));
    }

    @DeleteMapping("/delete/{ids}")
    @ApiOperation(value = "删除友情链接")
    @SaCheckPermission("sys:friend:delete")
    @OperationLogger(value = "友情链接",module = "管理端")
    @RepeatSubmit
    public Result<Object> delete(@PathVariable List<Integer> ids) {
        return Result.success(sysFriendService.removeBatchByIds(ids));
    }
}
