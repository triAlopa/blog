-- 添加测试用户的SQL脚本
-- 使用说明：
-- 1. 修改下面的用户名和密码
-- 2. 执行此SQL
-- 3. 使用新账号登录

-- 方案1：添加新测试用户
-- 密码：123456（BCrypt加密后的值）
INSERT INTO sys_user (username, password, nickname, status, create_time, update_time)
VALUES (
    'testuser',                                    -- 用户名（修改这里）
    '$2a$10$GSHv.XwqBkizplz5j2pcmu73IRY2rgtxCYQwAvSXMvu9SryzydLpe',  -- 密码：123456
    '测试用户',                                     -- 昵称
    1,                                              -- 状态：1=正常，0=禁用
    NOW(),
    NOW()
);

-- 获取新用户的ID
SET @new_user_id = LAST_INSERT_ID();

-- 给新用户分配角色（普通用户角色，role_id=2）
INSERT INTO sys_user_role (user_id, role_id) VALUES (@new_user_id, 2);

-- 验证用户创建成功
SELECT id, username, nickname, status FROM sys_user WHERE username = 'testuser';

-- ========================================
-- 方案2：重置现有admin用户的密码
-- 如果admin登录不上，可以重置密码
-- ========================================

-- 重置admin密码为 123456
-- UPDATE sys_user SET password = '$2a$10$GSHv.XwqBkizplz5j2pcmu73IRY2rgtxCYQwAvSXMvu9SryzydLpe' WHERE username = 'admin';

-- 验证admin用户
-- SELECT id, username, nickname, status FROM sys_user WHERE username = 'admin';
