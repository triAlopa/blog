@echo off
chcp 65001 >nul
echo ========================================
echo   Bangumi API 镜像站测试工具
echo ========================================
echo.
echo 测试说明：
echo - API 地址：用于获取追番数据
echo - 图片 CDN：用于加载封面图片
echo.
echo ========================================
echo.

echo [1] 测试官方 API: api.bgm.tv
curl -s -o nul -w "HTTP状态: %%{http_code}" "https://api.bgm.tv/v0/users/971837" --connect-timeout 5 --max-time 10 2>nul
echo.

echo [2] 测试官方 CDN: lain.bgm.tv
curl -s -o nul -w "HTTP状态: %%{http_code}" "https://lain.bgm.tv/pic/cover/c/f0/d0/1234.jpg" --connect-timeout 5 --max-time 10 2>nul
echo.

echo [3] 测试镜像: bangumi.one
curl -s -o nul -w "HTTP状态: %%{http_code}" "https://bangumi.one/v0/users/971837" --connect-timeout 5 --max-time 10 2>nul
echo.

echo [4] 测试镜像: bangumi.lol
curl -s -o nul -w "HTTP状态: %%{http_code}" "https://bangumi.lol/v0/users/971837" --connect-timeout 5 --max-time 10 2>nul
echo.

echo [5] 测试镜像: bangumi.rdd.moe
curl -s -o nul -w "HTTP状态: %%{http_code}" "https://bangumi.rdd.moe/v0/users/971837" --connect-timeout 5 --max-time 10 2>nul
echo.

echo [6] 测试镜像: bgmmi.anibt.net
curl -s -o nul -w "HTTP状态: %%{http_code}" "https://bgmmi.anibt.net/v0/users/971837" --connect-timeout 5 --max-time 10 2>nul
echo.

echo.
echo ========================================
echo 测试结果说明：
echo - HTTP状态 200 = 可用
echo - HTTP状态 404 = 路径不同，可能需要调整
echo - HTTP状态 000 = 无法连接
echo ========================================
echo.
echo 配置步骤：
echo 1. 选择可用的 API 镜像站（用于获取数据）
echo 2. 选择可用的图片 CDN（用于加载封面）
echo 3. 在后台管理中修改配置：
echo    系统管理 -^> 参数配置
echo    - bangumi_api_url（API地址）
echo    - bangumi_img_url（图片CDN地址）
echo.
pause
