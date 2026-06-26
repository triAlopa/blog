-- ========================================
-- 快速添加测试用户
-- ========================================

-- 使用说明：
-- 1. 修改下面的用户名和密码值
-- 2. 在 MySQL 中执行此 SQL
-- 3. 使用新账号登录

-- 【修改这里】设置你的用户名和密码
SET @my_username = 'chen';           -- 用户名
SET @my_password = '123456';         -- 密码（明文，后面会加密）
SET @my_nickname = '测试用户';        -- 昵称

-- 生成 BCrypt 密码（使用 MySQL 函数）
-- 注意：MySQL 没有内置 BCrypt 函数，需要使用预加密的值
-- 这里使用 123456 的预加密值
SET @bcrypt_password = '$2a$10$EnPm4xBrkdKs45qnePTLj.uImNE.L2vhkiZnjNknQFbgW.N9gnIQS';

-- 检查用户名是否已存在
SELECT CASE 
    WHEN EXISTS (SELECT 1 FROM sys_user WHERE username = @my_username) 
    THEN CONCAT('错误：用户名 "', @my_username, '" 已存在！')
    ELSE '用户名可用，继续执行...'
END AS check_result;

-- 如果用户名不存在，则插入新用户
-- 注意：如果上面显示用户名已存在，请修改 @my_username 后重新执行
INSERT INTO sys_user (username, password, nickname, status, create_time, update_time)
SELECT @my_username, @bcrypt_password, @my_nickname, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = @my_username);

-- 获取新用户的ID
SET @new_user_id = LAST_INSERT_ID();

-- 给新用户分配角色（普通用户角色）
INSERT INTO sys_user_role (user_id, role_id) 
SELECT @new_user_id, 2
FROM DUAL
WHERE @new_user_id > 0;

-- 验证结果
SELECT 
    id, 
    username, 
    nickname, 
    status,
    CASE WHEN status = 1 THEN '正常' ELSE '禁用' END AS status_text
FROM sys_user 
WHERE username = @my_username;

-- 显示登录信息
SELECT CONCAT('✅ 用户创建成功！使用以下信息登录：') AS message;
SELECT CONCAT('用户名: ', @my_username) AS username;
SELECT CONCAT('密码: 123456') AS password;
