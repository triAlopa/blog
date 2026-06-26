@echo off
chcp 65001 >nul
echo ========================================
echo   本地文件存储修复脚本
echo ========================================
echo.

echo [1/4] 创建存储目录...
if not exist "D:\Chen\Pictures\dev_temp" (
    mkdir "D:\Chen\Pictures\dev_temp"
    echo ✅ 目录创建成功：D:\Chen\Pictures\dev_temp
) else (
    echo ✅ 目录已存在：D:\Chen\Pictures\dev_temp
)
echo.

echo [2/4] 更新数据库配置...
echo 请在MySQL中执行以下SQL：
echo.
echo USE blog;
echo.
echo UPDATE sys_file_oss SET
echo   storage_path = 'D:/Chen/Pictures/dev_temp/',
echo   domain = 'http://127.0.0.1:8800/localFile/',
echo   path_patterns = 'localFile/**',
echo   base_path = 'local-plus/',
echo   is_enable = 1,
echo   enable_access = 1
echo WHERE platform = 'local';
echo.
echo 或者执行：fix-storage-path.sql
echo.
pause

echo [3/4] 检查MySQL配置...
echo 请确认已执行SQL更新，然后按任意键继续...
pause >nul

echo [4/4] 重启后端服务...
echo.
echo 请手动重启后端服务：
echo   1. 停止当前运行的服务（Ctrl+C）
echo   2. 执行：mvn spring-boot:run -pl mojian-server
echo.
echo 启动后验证：
echo   1. 访问 http://127.0.0.1:8800/shiyi/doc.html
echo   2. 访问 http://127.0.0.1:8800/localFile/test.jpg
echo      应该返回404（而不是401）
echo.

echo ========================================
echo 修复完成！请重启后端服务
echo ========================================
pause
