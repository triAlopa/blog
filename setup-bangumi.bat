@echo off
chcp 65001 >nul
echo ========================================
echo   Bangumi 追番功能配置
echo ========================================
echo.
echo API 地址已更新为: https://lain.bgm.tv
echo.

echo [步骤1] 执行数据库配置
echo.
echo 请在 MySQL 中执行：
echo ----------------------------------------
echo mysql -u root -p blog ^< bangumi-config.sql
echo ----------------------------------------
echo.
pause

echo [步骤2] 重启后端服务
echo.
echo cd C:\Users\Chen\Desktop\shiyi-blog\blog
echo mvn clean compile
echo mvn spring-boot:run -pl mojian-server
echo.
pause

echo [步骤3] 测试 API 连接
echo.
echo 访问：http://localhost:8800/bangumi/test
echo.
echo 预期返回：连接成功！
echo.
pause

echo [步骤4] 访问追番页面
echo.
echo 1. 访问 http://localhost:3000
echo 2. 点击导航栏的"关于"
echo 3. 点击"我的追番"标签页
echo.
echo ========================================
echo 完成！
echo ========================================
pause
