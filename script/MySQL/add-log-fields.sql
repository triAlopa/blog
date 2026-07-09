-- ----------------------------
-- 为操作日志表添加新字段
-- ----------------------------

ALTER TABLE `sys_operate_log`
    ADD COLUMN `duration_level` varchar(10) DEFAULT NULL COMMENT '耗时等级：fast/normal/slow' AFTER `spend_time`;

ALTER TABLE `sys_operate_log`
    ADD COLUMN `response_body` text DEFAULT NULL COMMENT '响应体内容' AFTER `duration_level`;

-- 创建索引，方便按耗时等级筛选
CREATE INDEX `idx_duration_level` ON `sys_operate_log` (`duration_level`);
