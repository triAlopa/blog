-- ----------------------------
-- 主题配色颜色字段
-- ----------------------------

-- 在 sys_web_config 表中添加主题颜色字段
ALTER TABLE `sys_web_config`
ADD COLUMN `theme_name` varchar(50) DEFAULT 'default' COMMENT '当前主题名称' AFTER `theme_presets`,
ADD COLUMN `primary_color` varchar(20) DEFAULT '#6366f1' COMMENT '主色调' AFTER `theme_name`,
ADD COLUMN `secondary_color` varchar(20) DEFAULT '#8b5cf6' COMMENT '次要色' AFTER `primary_color`,
ADD COLUMN `accent_color` varchar(20) DEFAULT '#f59e0b' COMMENT '强调色' AFTER `secondary_color`,
ADD COLUMN `bg_color` varchar(20) DEFAULT '#ffffff' COMMENT '背景色' AFTER `accent_color`,
ADD COLUMN `text_color` varchar(20) DEFAULT '#1f2937' COMMENT '文字色' AFTER `bg_color`,
ADD COLUMN `card_bg_color` varchar(20) DEFAULT '#ffffff' COMMENT '卡片背景色' AFTER `text_color`,
ADD COLUMN `gradient_start` varchar(20) DEFAULT '#6366f1' COMMENT '渐变开始色' AFTER `card_bg_color`,
ADD COLUMN `gradient_end` varchar(20) DEFAULT '#8b5cf6' COMMENT '渐变结束色' AFTER `gradient_start`,
ADD COLUMN `shadow_color` varchar(30) DEFAULT 'rgba(99,102,241,0.1)' COMMENT '阴影颜色' AFTER `gradient_end`;
