-- ========================================
-- 添加清理日志定时任务
-- ========================================

-- 添加清理操作日志任务（每月1号凌晨3点执行，清理30天前的日志）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_time, update_time, remark)
VALUES ('清理操作日志', 'DEFAULT', 'task.cleanOperateLog', '0 0 3 1 * ?', '3', '1', '0', NOW(), NOW(), '每月1号凌晨3点清理30天前的操作日志');

-- 添加清理用户日志任务（每月1号凌晨3点30分执行，清理30天前的日志）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_time, update_time, remark)
VALUES ('清理用户日志', 'DEFAULT', 'task.cleanUserLog', '0 30 3 1 * ?', '3', '1', '0', NOW(), NOW(), '每月1号凌晨3点30分清理30天前的用户日志');

-- 验证
SELECT job_id, job_name, invoke_target, cron_expression, remark FROM sys_job WHERE job_name LIKE '%日志%';
