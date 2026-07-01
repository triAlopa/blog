-- ========================================
-- 重置 admin 密码为 123456
-- ========================================

-- 使用说明：
-- 1. 在 MySQL 中执行此 SQL
-- 2. 使用 admin / 123456 登录

-- 更新 admin 密码为 123456
UPDATE sys_user 
SET password = '$2a$10$GSHv.XwqBkizplz5j2pcmu73IRY2rgtxCYQwAvSXMvu9SryzydLpe'
WHERE username = 'admin';

-- 验证更新结果
SELECT id, username, nickname, status FROM sys_user WHERE username = 'admin';

-- 显示登录信息
SELECT '✅ admin 密码已重置！' AS message;
SELECT '用户名: admin' AS username;
SELECT '密码: 123456' AS password;
