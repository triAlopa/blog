@echo off
chcp 65001 >nul
echo ========================================
echo   验证本地文件访问修复
echo ========================================
echo.

echo [步骤1] 请确保已执行SQL更新数据库配置
echo.
echo 在MySQL中执行：
echo ----------------------------------------
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
echo ----------------------------------------
echo.
echo 按任意键继续...
pause >nul

echo [步骤2] 检查目录
if exist "D:\Chen\Pictures\dev_temp" (
    echo ✅ 目录已存在：D:\Chen\Pictures\dev_temp
) else (
    echo 创建目录...
    mkdir "D:\Chen\Pictures\dev_temp"
    echo ✅ 目录创建成功
)
echo.

echo [步骤3] 代码已修改完成
echo.
echo 已修改的文件：
echo   1. WebMvcConfig.java - 硬编码本地文件资源处理器
echo   2. SaTokenConfigure.java - 添加配置日志
echo.
echo 按任意键继续...
pause >nul

echo [步骤4] 重启后端服务
echo.
echo 请执行以下步骤：
echo   1. 停止当前后端服务（Ctrl+C）
echo   2. 清理编译：mvn clean compile
echo   3. 启动服务：mvn spring-boot:run -pl mojian-server
echo.
echo 启动时应该看到以下日志：
echo ----------------------------------------
echo 注册本地文件资源处理器:
echo   路径模式: localFile/**
echo   存储位置: file:D:/Chen/Pictures/dev_temp/
echo.
echo 注册 Sa-Token 拦截器...
echo 排除路径: /localFile/**
echo ----------------------------------------
echo.
echo 按任意键继续验证...
pause >nul

echo [步骤5] 验证修复
echo.
echo 请在浏览器中访问：
echo http://127.0.0.1:8800/localFile/test.jpg
echo.
echo 预期结果：
echo   - 返回 404 Not Found ✅ (配置正确)
echo   - 返回 401 Unauthorized ❌ (配置未生效)
echo.
echo 如果返回401，请检查：
echo   1. 启动日志是否显示资源处理器注册信息
echo   2. 数据库配置是否正确更新
echo   3. 是否重新编译了代码
echo.

echo ========================================
echo 验证完成！
echo ========================================
pause
