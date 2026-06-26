package com.mojian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mojian.entity.SysOperateLog;
import com.mojian.mapper.SysOperateLogMapper;
import com.mojian.service.SysOperateLogService;
import com.mojian.utils.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysOperateLogServiceImpl extends ServiceImpl<SysOperateLogMapper, SysOperateLog> implements SysOperateLogService {

    /**
     * 查询分页列表
     */
    @Override
    public IPage<SysOperateLog> listSysOperateLog(SysOperateLog sysOperateLog) {
        LambdaQueryWrapper<SysOperateLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(sysOperateLog.getUsername() != null, SysOperateLog::getUsername, sysOperateLog.getUsername());
        wrapper.like(sysOperateLog.getOperationName() != null, SysOperateLog::getOperationName, sysOperateLog.getOperationName());
        wrapper.eq(sysOperateLog.getType() != null, SysOperateLog::getType, sysOperateLog.getType());
        wrapper.eq(sysOperateLog.getRequestMethod() != null, SysOperateLog::getRequestMethod, sysOperateLog.getRequestMethod());
        wrapper.eq(sysOperateLog.getModule() != null, SysOperateLog::getModule, sysOperateLog.getModule());
        wrapper.orderByDesc(SysOperateLog::getCreateTime);
        return page(PageUtil.getPage(), wrapper);
    }

    /**
     * 设备分布统计
     */
    @Override
    public List<Map<String, Object>> getDeviceStatistics() {
        List<SysOperateLog> list = list();
        Map<String, Long> deviceMap = list.stream()
                .filter(log -> log.getDeviceType() != null)
                .collect(Collectors.groupingBy(SysOperateLog::getDeviceType, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        deviceMap.forEach((key, value) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", key);
            item.put("value", value);
            result.add(item);
        });
        return result;
    }

    /**
     * 浏览器分布统计
     */
    @Override
    public List<Map<String, Object>> getBrowserStatistics() {
        List<SysOperateLog> list = list();
        Map<String, Long> browserMap = list.stream()
                .filter(log -> log.getBrowser() != null)
                .collect(Collectors.groupingBy(SysOperateLog::getBrowser, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        browserMap.forEach((key, value) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", key);
            item.put("value", value);
            result.add(item);
        });
        return result;
    }

    /**
     * 操作系统分布统计
     */
    @Override
    public List<Map<String, Object>> getOsStatistics() {
        List<SysOperateLog> list = list();
        Map<String, Long> osMap = list.stream()
                .filter(log -> log.getOs() != null)
                .collect(Collectors.groupingBy(SysOperateLog::getOs, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        osMap.forEach((key, value) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", key);
            item.put("value", value);
            result.add(item);
        });
        return result;
    }

    /**
     * 地区分布统计
     */
    @Override
    public List<Map<String, Object>> getRegionStatistics() {
        List<SysOperateLog> list = list();

        // 省份简称到全称的映射
        Map<String, String> provinceMap = new HashMap<>();
        provinceMap.put("北京", "北京市");
        provinceMap.put("上海", "上海市");
        provinceMap.put("天津", "天津市");
        provinceMap.put("重庆", "重庆市");
        provinceMap.put("河北", "河北省");
        provinceMap.put("山西", "山西省");
        provinceMap.put("辽宁", "辽宁省");
        provinceMap.put("吉林", "吉林省");
        provinceMap.put("黑龙江", "黑龙江省");
        provinceMap.put("江苏", "江苏省");
        provinceMap.put("浙江", "浙江省");
        provinceMap.put("安徽", "安徽省");
        provinceMap.put("福建", "福建省");
        provinceMap.put("江西", "江西省");
        provinceMap.put("山东", "山东省");
        provinceMap.put("河南", "河南省");
        provinceMap.put("湖北", "湖北省");
        provinceMap.put("湖南", "湖南省");
        provinceMap.put("广东", "广东省");
        provinceMap.put("海南", "海南省");
        provinceMap.put("四川", "四川省");
        provinceMap.put("贵州", "贵州省");
        provinceMap.put("云南", "云南省");
        provinceMap.put("陕西", "陕西省");
        provinceMap.put("甘肃", "甘肃省");
        provinceMap.put("青海", "青海省");
        provinceMap.put("台湾", "台湾省");
        provinceMap.put("内蒙古", "内蒙古自治区");
        provinceMap.put("广西", "广西壮族自治区");
        provinceMap.put("西藏", "西藏自治区");
        provinceMap.put("宁夏", "宁夏回族自治区");
        provinceMap.put("新疆", "新疆维吾尔自治区");
        provinceMap.put("香港", "香港特别行政区");
        provinceMap.put("澳门", "澳门特别行政区");

        Map<String, Long> regionMap = list.stream()
                .filter(log -> log.getIpSource() != null && !log.getIpSource().isEmpty())
                .collect(Collectors.groupingBy(log -> {
                    // 提取省份名称并转换为全称
                    String source = log.getIpSource();
                    String province = source;
                    if (source.contains("|")) {
                        province = source.split("\\|")[1];
                    }
                    // 转换为全称
                    return provinceMap.getOrDefault(province, province);
                }, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        regionMap.forEach((key, value) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", key);
            item.put("value", value);
            result.add(item);
        });
        return result;
    }

    /**
     * 访问趋势统计
     */
    @Override
    public List<Map<String, Object>> getVisitTrend(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<SysOperateLog> list = list(new LambdaQueryWrapper<SysOperateLog>()
                .ge(SysOperateLog::getCreateTime, startDate.atStartOfDay())
                .le(SysOperateLog::getCreateTime, endDate.atTime(LocalTime.MAX)));

        // 按日期分组
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        Map<String, Long> dateMap = list.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getCreateTime().format(formatter),
                        Collectors.counting()
                ));

        // 生成连续日期
        List<Map<String, Object>> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> item = new HashMap<>();
            String dateStr = date.format(formatter);
            item.put("date", dateStr);
            item.put("count", dateMap.getOrDefault(dateStr, 0L));
            result.add(item);
        }
        return result;
    }

    /**
     * 统计摘要
     */
    @Override
    public Map<String, Object> getStatsSummary() {
        Map<String, Object> summary = new HashMap<>();

        // 今日访问量
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = count(new LambdaQueryWrapper<SysOperateLog>()
                .ge(SysOperateLog::getCreateTime, todayStart));
        summary.put("todayCount", todayCount);

        // 总访问量
        long totalCount = count();
        summary.put("totalCount", totalCount);

        // 今日用户数
        long todayUserCount = list(new LambdaQueryWrapper<SysOperateLog>()
                .ge(SysOperateLog::getCreateTime, todayStart)
                .eq(SysOperateLog::getType, "user"))
                .stream()
                .map(SysOperateLog::getUsername)
                .distinct()
                .count();
        summary.put("todayUserCount", todayUserCount);

        // 总用户数
        long totalUserCount = list(new LambdaQueryWrapper<SysOperateLog>()
                .eq(SysOperateLog::getType, "user"))
                .stream()
                .map(SysOperateLog::getUsername)
                .distinct()
                .count();
        summary.put("totalUserCount", totalUserCount);

        return summary;
    }
}
