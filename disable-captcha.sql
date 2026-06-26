-- 关闭滑块验证码
UPDATE sys_config SET config_value = 'N' WHERE config_key = 'slider_verify_switch';

-- 验证配置
SELECT config_key, config_value, remark FROM sys_config WHERE config_key = 'slider_verify_switch';
