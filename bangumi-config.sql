-- ========================================
-- Bangumi 追番配置（使用反代地址）
-- ========================================

-- 删除旧的配置（如果存在）
DELETE FROM sys_config WHERE config_key LIKE 'bangumi_%';

-- 插入新的配置
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, create_time, update_time)
VALUES
    ('Bangumi用户ID', 'bangumi_user_id', '971837', 'Y', 'Bangumi 网站的用户ID', NOW(), NOW()),
    ('Bangumi API地址', 'bangumi_api_url', 'https://bgmapi.anibt.net', 'Y', 'Bangumi API反代地址（用于获取数据）', NOW(), NOW()),
    ('Bangumi图片地址', 'bangumi_img_url', 'https://bgmimg.anibt.net', 'Y', 'Bangumi 图片反代地址（用于加载封面）', NOW(), NOW()),
    ('启用追番页面', 'bangumi_enable', 'Y', 'Y', '是否启用追番页面（Y/N）', NOW(), NOW());

-- 验证配置
SELECT config_key, config_name, config_value, remark 
FROM sys_config 
WHERE config_key LIKE 'bangumi_%';
