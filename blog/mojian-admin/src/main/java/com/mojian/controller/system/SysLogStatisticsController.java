package com.mojian.controller.system;

import com.mojian.common.Result;
import com.mojian.service.SysOperateLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 日志统计控制器
 */
@RestController
@RequestMapping("/sys/log/statistics")
@RequiredArgsConstructor
@Api(tags = "日志统计")
public class SysLogStatisticsController {

    private final SysOperateLogService sysOperateLogService;

    @GetMapping("/device")
    @ApiOperation(value = "设备分布统计")
    public Result<List<Map<String, Object>>> getDeviceStatistics() {
        return Result.success(sysOperateLogService.getDeviceStatistics());
    }

    @GetMapping("/browser")
    @ApiOperation(value = "浏览器分布统计")
    public Result<List<Map<String, Object>>> getBrowserStatistics() {
        return Result.success(sysOperateLogService.getBrowserStatistics());
    }

    @GetMapping("/os")
    @ApiOperation(value = "操作系统分布统计")
    public Result<List<Map<String, Object>>> getOsStatistics() {
        return Result.success(sysOperateLogService.getOsStatistics());
    }

    @GetMapping("/region")
    @ApiOperation(value = "地区分布统计")
    public Result<List<Map<String, Object>>> getRegionStatistics() {
        return Result.success(sysOperateLogService.getRegionStatistics());
    }

    @GetMapping("/trend")
    @ApiOperation(value = "访问趋势统计")
    public Result<List<Map<String, Object>>> getVisitTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(sysOperateLogService.getVisitTrend(days));
    }

    @GetMapping("/summary")
    @ApiOperation(value = "统计摘要")
    public Result<Map<String, Object>> getStatsSummary() {
        return Result.success(sysOperateLogService.getStatsSummary());
    }
}
