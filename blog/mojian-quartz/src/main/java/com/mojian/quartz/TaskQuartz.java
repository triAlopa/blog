package com.mojian.quartz;

import com.mojian.common.RedisConstants;
import com.mojian.entity.SysArticle;
import com.mojian.mapper.SysArticleMapper;
import com.mojian.mapper.SysOperateLogMapper;
import com.mojian.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojian.entity.SysOperateLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 定时任务
 */
@Slf4j
@Component("task")
@RequiredArgsConstructor
public class TaskQuartz {

    private final RedisUtil redisUtil;

    private final SysArticleMapper articleMapper;

    private final SysOperateLogMapper operateLogMapper;

    public void neatMultipleParams(String s, Boolean b, Long l, Double d, Integer i) {
        // 多参方法
    }

    public void neatParams(String params) {
        System.out.println("执行有参方法：" + params);
    }

    public void neatNoParams() {
        System.out.println("执行无参方法");
    }

    /**
     * 定时同步阅读量
     */
    public void syncQuantity() {
        // 获取带阅读量的前缀key集合
        List<SysArticle> articles = new ArrayList<>();
        Map<Object, Object> map = redisUtil.hGetAll(RedisConstants.ARTICLE_QUANTITY);
        // 取出所有数据更新到数据库
        for (Map.Entry<Object, Object> stringEntry : map.entrySet()) {
            Object id = stringEntry.getKey();
            List<String> list = (List<String>) stringEntry.getValue();
            SysArticle article = SysArticle.builder()
                    .id(Long.parseLong(id.toString())).quantity(list.size())
                    .build();
            articles.add(article);
        }
        articleMapper.updateBatchQuantity(articles);
    }

    /**
     * 清理操作日志（默认清理30天前的数据）
     */
    public void cleanOperateLog() {
        cleanOperateLog(30);
    }

    /**
     * 清理操作日志（指定天数）
     * @param days 保留最近几天的日志
     */
    public void cleanOperateLog(int days) {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
            int count = operateLogMapper.delete(
                    new LambdaQueryWrapper<SysOperateLog>()
                            .lt(SysOperateLog::getCreateTime, expireTime)
            );
            log.info("清理操作日志成功，删除 {} 天前的日志 {} 条", days, count);
        } catch (Exception e) {
            log.error("清理操作日志失败", e);
        }
    }

    /**
     * 清理用户日志（默认清理30天前的数据）
     */
    public void cleanUserLog() {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(30);
            int count = operateLogMapper.delete(
                    new LambdaQueryWrapper<SysOperateLog>()
                            .eq(SysOperateLog::getType, "user")
                            .lt(SysOperateLog::getCreateTime, expireTime)
            );
            log.info("清理用户日志成功，删除 30 天前的日志 {} 条", count);
        } catch (Exception e) {
            log.error("清理用户日志失败", e);
        }
    }
}
