package com.mojian.controller.system;

import java.util.List;
import java.util.Map;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import com.mojian.entity.SysOperateLog;
import com.mojian.service.SysOperateLogService;
import com.mojian.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/sys/operateLog")
@RequiredArgsConstructor
@Api(tags = "操作日志管理")
public class SysOperateLogController {

    private final SysOperateLogService sysOperateLogService;

    @GetMapping
    @ApiOperation(value = "获取操作日志列表")
    public Result<IPage<SysOperateLog>> list(SysOperateLog sysOperateLog) {
        return Result.success(sysOperateLogService.listSysOperateLog(sysOperateLog));
    }

    @DeleteMapping("delete/{ids}")
    @ApiOperation(value = "批量删除操作日志")
    @SaCheckPermission("sys:operateLog:delete")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysOperateLogService.removeBatchByIds(ids);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @ApiOperation(value = "清空操作日志")
    @SaCheckPermission("sys:operateLog:delete")
    public Result<Void> clean() {
        sysOperateLogService.remove(new LambdaQueryWrapper<>());
        return Result.success();
    }

    @GetMapping("/statistics/device")
    @ApiOperation(value = "设备分布统计")
    public Result<List<Map<String, Object>>> getDeviceStatistics() {
        return Result.success(sysOperateLogService.getDeviceStatistics());
    }

    @GetMapping("/statistics/browser")
    @ApiOperation(value = "浏览器分布统计")
    public Result<List<Map<String, Object>>> getBrowserStatistics() {
        return Result.success(sysOperateLogService.getBrowserStatistics());
    }

    @GetMapping("/statistics/os")
    @ApiOperation(value = "操作系统分布统计")
    public Result<List<Map<String, Object>>> getOsStatistics() {
        return Result.success(sysOperateLogService.getOsStatistics());
    }

    @GetMapping("/statistics/region")
    @ApiOperation(value = "地区分布统计")
    public Result<List<Map<String, Object>>> getRegionStatistics() {
        return Result.success(sysOperateLogService.getRegionStatistics());
    }

    @GetMapping("/statistics/trend")
    @ApiOperation(value = "访问趋势统计")
    public Result<List<Map<String, Object>>> getVisitTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(sysOperateLogService.getVisitTrend(days));
    }

    @GetMapping("/statistics/summary")
    @ApiOperation(value = "统计摘要")
    public Result<Map<String, Object>> getStatsSummary() {
        return Result.success(sysOperateLogService.getStatsSummary());
    }
}
