-- ----------------------------
-- 敏感词管理模块
-- ----------------------------

-- ----------------------------
-- Table structure for sys_sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `sys_sensitive_word`;
CREATE TABLE `sys_sensitive_word`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `word`        varchar(100) NOT NULL COMMENT '敏感词内容',
    `word_type`   varchar(20) NOT NULL DEFAULT 'CUSTOM' COMMENT '类型: CUSTOM-自定义 OFFICIAL-官方',
    `status`      varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用 DISABLED-禁用',
    `create_by`   bigint       DEFAULT NULL COMMENT '创建者',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint       DEFAULT NULL COMMENT '更新者',
    `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_word` (`word`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '敏感词表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_sensitive_word_whitelist
-- ----------------------------
DROP TABLE IF EXISTS `sys_sensitive_word_whitelist`;
CREATE TABLE `sys_sensitive_word_whitelist`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `word`        varchar(100) NOT NULL COMMENT '白名单词',
    `reason`      varchar(255) DEFAULT NULL COMMENT '添加原因',
    `create_by`   bigint       DEFAULT NULL COMMENT '创建者',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint       DEFAULT NULL COMMENT '更新者',
    `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_word` (`word`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '敏感词白名单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 初始化一些常用敏感词（示例）
-- ----------------------------
INSERT INTO `sys_sensitive_word` (`word`, `word_type`, `status`) VALUES
('fuck', 'OFFICIAL', 'ACTIVE'),
('shit', 'OFFICIAL', 'ACTIVE'),
('ass', 'OFFICIAL', 'ACTIVE');

-- ----------------------------
-- 菜单数据 - 敏感词管理
-- ----------------------------

-- 敏感词管理菜单（父菜单ID=1为系统管理）
INSERT INTO `sys_menu` (`parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `create_time`, `update_time`, `redirect`, `name`, `hidden`, `perm`, `is_external`)
VALUES ('1', 'sensitive', '/system/sensitive/index', '敏感词管理', 4, 'Warning', 'MENU', '2026-07-07 00:00:00', NULL, '', '', 0, '', 0);

-- 获取刚插入的敏感词管理菜单ID
SET @sensitive_menu_id = LAST_INSERT_ID();

-- 敏感词管理按钮权限
INSERT INTO `sys_menu` (`parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `create_time`, `update_time`, `redirect`, `name`, `hidden`, `perm`, `is_external`)
VALUES (@sensitive_menu_id, '', '', '查看敏感词', 1, '', 'BUTTON', '2026-07-07 00:00:00', NULL, '', '', 1, 'system:sensitive:list', 0);

INSERT INTO `sys_menu` (`parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `create_time`, `update_time`, `redirect`, `name`, `hidden`, `perm`, `is_external`)
VALUES (@sensitive_menu_id, '', '', '新增敏感词', 2, '', 'BUTTON', '2026-07-07 00:00:00', NULL, '', '', 1, 'system:sensitive:add', 0);

INSERT INTO `sys_menu` (`parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `create_time`, `update_time`, `redirect`, `name`, `hidden`, `perm`, `is_external`)
VALUES (@sensitive_menu_id, '', '', '删除敏感词', 3, '', 'BUTTON', '2026-07-07 00:00:00', NULL, '', '', 1, 'system:sensitive:delete', 0);

INSERT INTO `sys_menu` (`parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `create_time`, `update_time`, `redirect`, `name`, `hidden`, `perm`, `is_external`)
VALUES (@sensitive_menu_id, '', '', '刷新词库', 4, '', 'BUTTON', '2026-07-07 00:00:00', NULL, '', '', 1, 'system:sensitive:reload', 0);
