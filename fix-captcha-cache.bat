@echo off
chcp 65001 >nul
echo ========================================
echo   清除滑块验证码缓存
echo ========================================
echo.

echo [步骤1] 确认数据库配置
echo.
echo 请在MySQL中执行：
echo ----------------------------------------
echo SELECT config_key, config_value FROM sys_config WHERE config_key = 'slider_verify_switch';
echo ----------------------------------------
echo.
echo 预期结果：config_value 应该是 N
echo.
pause

echo [步骤2] 清除Redis缓存
echo.
echo 请在Redis中执行：
echo ----------------------------------------
echo SELECT 8
echo KEYS *slider_verify_switch*
echo DEL sys_config::slider_verify_switch
echo ----------------------------------------
echo.
echo 或者清除所有缓存（谨慎使用）：
echo FLUSHDB
echo.
pause

echo [步骤3] 重启后端服务
echo.
echo 请重启后端服务，然后测试登录
echo.

echo ========================================
echo 完成！
echo ========================================
pause
