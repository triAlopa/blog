@echo off
chcp 65001 >nul
echo ========================================
echo   登录测试脚本
echo ========================================
echo.

echo [步骤1] 重置 admin 密码
echo.
echo 请在 MySQL 中执行：
echo ----------------------------------------
echo USE blog;
echo.
echo UPDATE sys_user SET password = '$2a$10$GSHv.XwqBkizplz5j2pcmu73IRY2rgtxCYQwAvSXMvu9SryzydLpe' WHERE username = 'admin';
echo ----------------------------------------
echo.
pause

echo [步骤2] 检查用户状态
echo.
echo 请在 MySQL 中执行：
echo ----------------------------------------
echo SELECT id, username, nickname, status FROM sys_user WHERE username = 'admin';
echo ----------------------------------------
echo.
echo 确保 status = 1（正常状态）
echo.
pause

echo [步骤3] 测试登录
echo.
echo 使用以下信息登录：
echo   用户名: admin
echo   密码: 123456
echo.
echo 登录地址: http://localhost:3000
echo.

echo [步骤4] 如果还是失败
echo.
echo 检查以下内容：
echo   1. 后端服务是否启动（http://localhost:8800）
echo   2. 滑块验证码是否已关闭
echo   3. 控制台是否有错误信息
echo.

echo ========================================
echo 完成！
echo ========================================
pause
