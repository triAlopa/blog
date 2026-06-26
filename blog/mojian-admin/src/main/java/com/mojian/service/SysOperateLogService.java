package com.mojian.service;

import com.mojian.entity.SysOperateLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 操作日志服务接口
 */
public interface SysOperateLogService extends IService<SysOperateLog> {

    /**
     * 查询分页列表
     */
    IPage<SysOperateLog> listSysOperateLog(SysOperateLog sysOperateLog);

    /**
     * 设备分布统计
     */
    List<Map<String, Object>> getDeviceStatistics();

    /**
     * 浏览器分布统计
     */
    List<Map<String, Object>> getBrowserStatistics();

    /**
     * 操作系统分布统计
     */
    List<Map<String, Object>> getOsStatistics();

    /**
     * 地区分布统计
     */
    List<Map<String, Object>> getRegionStatistics();

    /**
     * 访问趋势统计
     */
    List<Map<String, Object>> getVisitTrend(int days);

    /**
     * 统计摘要
     */
    Map<String, Object> getStatsSummary();
}
