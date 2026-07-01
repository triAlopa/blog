-- ========================================
-- 操作日志表重构
-- ========================================

-- 删除旧表（备份数据后再执行）
-- DROP TABLE IF EXISTS `sys_operate_log`;

-- 重建操作日志表
CREATE TABLE IF NOT EXISTS `sys_operate_log` (
    `id`              bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type`            varchar(20) DEFAULT 'admin' COMMENT '日志类型：admin=管理端, user=用户端',
    `username`        varchar(100) DEFAULT NULL COMMENT '操作用户',
    `operation_name`  varchar(255) DEFAULT NULL COMMENT '操作名称',
    `module`          varchar(50) DEFAULT NULL COMMENT '模块名称',
    `request_url`     varchar(500) DEFAULT NULL COMMENT '请求接口',
    `request_method`  varchar(10) DEFAULT NULL COMMENT '请求方式（GET/POST/PUT/DELETE）',
    `request_params`  text DEFAULT NULL COMMENT '请求参数',
    `response_code`   int DEFAULT NULL COMMENT '响应状态码',
    `error_msg`       text DEFAULT NULL COMMENT '错误信息',
    `ip`              varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `ip_source`       varchar(255) DEFAULT NULL COMMENT 'IP来源（省份/城市）',
    `user_agent`      varchar(500) DEFAULT NULL COMMENT 'User-Agent',
    `device_type`     varchar(20) DEFAULT NULL COMMENT '设备类型（PC/Mobile/Tablet）',
    `os`              varchar(50) DEFAULT NULL COMMENT '操作系统',
    `browser`         varchar(50) DEFAULT NULL COMMENT '浏览器类型',
    `spend_time`      bigint DEFAULT NULL COMMENT '请求耗时（毫秒）',
    `class_path`      varchar(255) DEFAULT NULL COMMENT '类路径',
    `method_name`     varchar(100) DEFAULT NULL COMMENT '方法名',
    `create_time`     datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_username` (`username`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_ip` (`ip`),
    KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ========================================
-- 添加用户日志菜单（在日志管理子级，和操作日志平级）
-- ========================================

-- 先查看日志管理菜单ID
-- SELECT id, title FROM sys_menu WHERE title = '日志管理';

-- 假设日志管理ID是32，添加用户日志菜单
INSERT INTO sys_menu (parent_id, path, component, title, sort, icon, type, create_time, update_time, hidden, perm, is_external)
VALUES (32, 'user-log', '/system/log/user-log/index', '用户日志', 2, 'User', 'MENU', NOW(), NOW(), 0, '', 0);

-- 获取新插入的菜单ID
SET @user_log_menu_id = LAST_INSERT_ID();

-- 添加用户日志的按钮权限
INSERT INTO sys_menu (parent_id, path, component, title, sort, icon, type, create_time, update_time, hidden, perm, is_external)
VALUES
(@user_log_menu_id, '', '', '列表', 1, '', 'BUTTON', NOW(), NOW(), 1, 'log:userLog:list', 0),
(@user_log_menu_id, '', '', '删除', 2, '', 'BUTTON', NOW(), NOW(), 1, 'log:userLog:delete', 0),
(@user_log_menu_id, '', '', '清空', 3, '', 'BUTTON', NOW(), NOW(), 1, 'log:userLog:clean', 0);

-- ========================================
-- 添加统计面板菜单（可选）
-- ========================================

-- 如果需要在监控中心添加统计面板菜单
 INSERT INTO sys_menu (parent_id, path, component, title, sort, icon, type, create_time, update_time, hidden, perm, is_external)
 VALUES (4, 'statistics', '/monitor/statistics/index', '访问统计', 5, 'TrendCharts', 'MENU', NOW(), NOW(), 0, '', 0);

-- 验证
SELECT id, parent_id, path, title, type, perm FROM sys_menu WHERE parent_id = 32;
