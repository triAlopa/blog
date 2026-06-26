@echo off
chcp 65001 >nul
echo ========================================
echo   修复本地文件访问401错误
echo ========================================
echo.

echo [步骤1] 更新数据库配置
echo.
echo 请在MySQL中执行以下SQL：
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
echo 或者执行：fix-local-access.sql
echo.
pause

echo [步骤2] 检查目录
if exist "D:\Chen\Pictures\dev_temp" (
    echo ✅ 目录已存在：D:\Chen\Pictures\dev_temp
) else (
    echo 创建目录...
    mkdir "D:\Chen\Pictures\dev_temp"
    if exist "D:\Chen\Pictures\dev_temp" (
        echo ✅ 目录创建成功
    ) else (
        echo ❌ 目录创建失败，请手动创建
    )
)
echo.

echo [步骤3] 检查代码配置
echo.
echo 请确认以下文件配置正确：
echo.
echo 1. WebMvcConfig.java (blog/mojian-commom/src/main/java/com/mojian/config/)
echo    - 确保从数据库读取本地存储配置
echo    - 或者临时硬编码：registry.addResourceHandler("localFile/**")
echo                                      .addResourceLocations("file:D:/Chen/Pictures/dev_temp/");
echo.
echo 2. SaTokenConfigure.java (blog/mojian-auth/src/main/java/com/mojian/config/satoken/)
echo    - 确保排除路径包含："/localFile/**"
echo.
echo 按任意键继续...
pause >nul

echo [步骤4] 重启后端服务
echo.
echo 请手动执行以下步骤：
echo   1. 停止当前后端服务（Ctrl+C）
echo   2. 清理编译：mvn clean compile
echo   3. 重新启动：mvn spring-boot:run -pl mojian-server
echo.
echo 启动后验证：
echo   访问 http://127.0.0.1:8800/localFile/test.jpg
echo   预期结果：404 Not Found（不是401 Unauthorized）
echo.

echo ========================================
echo 修复完成！请重启后端服务
echo ========================================
pause
