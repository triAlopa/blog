-- ========================================
-- Bangumi 追番配置
-- ========================================

-- 使用说明：
-- 1. 执行此 SQL 添加 Bangumi 配置
-- 2. 在后台管理中可以修改这些配置
-- 3. API 和图片需要分别配置代理地址

-- 添加 Bangumi 配置
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_time, update_time)
VALUES
    ('Bangumi用户ID', 'bangumi_user_id', '971837', 'Y', 'Bangumi 网站的用户ID', NOW(), NOW()),
    ('Bangumi API地址', 'bangumi_api_url', 'https://api.bgm.tv', 'Y', 'Bangumi API地址，可配置为镜像站地址', NOW(), NOW()),
    ('Bangumi图片地址', 'bangumi_img_url', 'https://lain.bgm.tv', 'Y', 'Bangumi 图片CDN地址，可配置为镜像站地址', NOW(), NOW()),
    ('启用追番页面', 'bangumi_enable', 'Y', 'Y', '是否启用追番页面（Y/N）', NOW(), NOW());

-- 验证配置
SELECT config_key, config_name, config_value, remark
FROM sys_config
WHERE config_key LIKE 'bangumi_%';

-- ========================================
-- 可用的 Bangumi 镜像站
-- 如果默认地址无法访问，请逐个尝试以下地址
-- ========================================

-- API 镜像站（用于获取数据）
-- UPDATE sys_config SET config_value = 'https://api.bgm.tv' WHERE config_key = 'bangumi_api_url';
-- UPDATE sys_config SET config_value = 'https://lain.bgm.tv' WHERE config_key = 'bangumi_api_url';
-- UPDATE sys_config SET config_value = 'https://bangumi.one' WHERE config_key = 'bangumi_api_url';
-- UPDATE sys_config SET config_value = 'https://bangumi.lol' WHERE config_key = 'bangumi_api_url';
-- UPDATE sys_config SET config_value = 'https://bangumi.rdd.moe' WHERE config_key = 'bangumi_api_url';
-- UPDATE sys_config SET config_value = 'https://bgmmi.anibt.net' WHERE config_key = 'bangumi_api_url';

-- 图片镜像站（用于加载封面图片）
-- UPDATE sys_config SET config_value = 'https://lain.bgm.tv' WHERE config_key = 'bangumi_img_url';
-- UPDATE sys_config SET config_value = 'https://bangumi.one' WHERE config_key = 'bangumi_img_url';
-- UPDATE sys_config SET config_value = 'https://bangumi.lol' WHERE config_key = 'bangumi_img_url';
-- UPDATE sys_config SET config_value = 'https://bangumi.rdd.moe' WHERE config_key = 'bangumi_img_url';
-- UPDATE sys_config SET config_value = 'https://bgmmi.anibt.net' WHERE config_key = 'bangumi_img_url';
