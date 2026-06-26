# Bangumi 追番功能配置指南

## 📋 功能说明

追番功能已集成到"关于本站"页面，作为标签页展示：
- **关于我**：显示原有的关于我内容
- **我的追番**：显示 Bangumi 追番数据

---

## 🔑 重要：两个代理地址

追番功能需要配置 **两个** 代理地址：

| 配置项 | 配置键 | 用途 | 示例值 |
|--------|--------|------|--------|
| API 地址 | `bangumi_api_url` | 获取追番数据 | `https://api.bgm.tv` |
| 图片 CDN | `bangumi_img_url` | 加载封面图片 | `https://lain.bgm.tv` |

**为什么需要两个？**
- API 地址用于获取追番列表、用户信息等数据
- 图片 CDN 用于加载动漫封面图片
- 两者可以使用不同的镜像站

---

## 🚀 配置步骤

### 步骤1：执行数据库配置

```bash
mysql -u root -p blog < bangumi-config.sql
```

### 步骤2：测试镜像站可用性

运行测试脚本：
```bash
test-bangumi-api.bat
```

测试脚本会分别测试：
- API 地址（用于获取数据）
- 图片 CDN（用于加载封面）

### 步骤3：配置代理地址

根据测试结果，在后台管理中修改配置：

**路径**：系统管理 → 参数配置

**配置项**：

| 配置键 | 说明 | 可选地址 |
|--------|------|----------|
| `bangumi_api_url` | API 地址 | 见下方列表 |
| `bangumi_img_url` | 图片 CDN | 见下方列表 |
| `bangumi_user_id` | 用户ID | `971837` |
| `bangumi_enable` | 启用页面 | `Y` |

**可选的镜像站地址**：

| 镜像站 | API 地址 | 图片 CDN |
|--------|----------|----------|
| 官方 | `https://api.bgm.tv` | `https://lain.bgm.tv` |
| bangumi.one | `https://bangumi.one` | `https://bangumi.one` |
| bangumi.lol | `https://bangumi.lol` | `https://bangumi.lol` |
| bangumi.rdd.moe | `https://bangumi.rdd.moe` | `https://bangumi.rdd.moe` |
| bgmmi.anibt.net | `https://bgmmi.anibt.net` | `https://bgmmi.anibt.net` |

### 步骤4：重启后端服务

```bash
cd C:\Users\Chen\Desktop\shiyi-blog\blog
mvn spring-boot:run -pl mojian-server
```

### 步骤5：测试 API 连接

访问：http://localhost:8800/bangumi/test

**预期返回**：
```json
{
  "code": 200,
  "message": "success",
  "data": "连接成功！\nAPI地址: https://xxx\n图片CDN: https://xxx\n用户ID: 971837"
}
```

### 步骤6：访问追番页面

1. 访问 http://localhost:3000
2. 点击导航栏的"关于"
3. 点击"我的追番"标签页

---

## 🎨 页面特点

- ✅ 竖屏封面卡片（2:3 比例）
- ✅ 5列响应式网格布局
- ✅ 状态标签（在看/看过/想看/搁置/抛弃）
- ✅ 评分标签（⭐ 图标）
- ✅ 进度条（仅在看状态显示）
- ✅ 过滤功能
- ✅ 暗色主题适配
- ✅ 响应式设计

---

## ❓ 常见问题

### Q1: 数据加载成功但图片不显示

**原因**：图片 CDN 配置错误

**解决方案**：
1. 检查 `bangumi_img_url` 配置
2. 确保图片 CDN 可访问
3. 尝试其他图片 CDN 地址

### Q2: API 返回 404

**原因**：镜像站路径格式不同

**解决方案**：
1. 尝试其他镜像站
2. 检查镜像站文档
3. 使用官方 API + 代理

### Q3: 追番数据为空

**原因**：
1. 用户ID配置错误
2. Bangumi 账号没有追番数据
3. API 返回数据格式变化

**解决方案**：
1. 检查 `bangumi_user_id` 配置
2. 在 Bangumi 网站添加追番数据
3. 查看后端日志

### Q4: 所有镜像站都无法访问

**原因**：网络环境限制

**解决方案**：
1. 使用 VPN 或代理
2. 自建反向代理
3. 联系网络管理员

---

## 🔧 自建反向代理

如果所有镜像站都不可用，可以自建反向代理：

### Nginx 配置示例

#### API 代理

```nginx
server {
    listen 443 ssl;
    server_name bangumi-api.yourdomain.com;

    location / {
        proxy_pass https://api.bgm.tv;
        proxy_set_header Host api.bgm.tv;
        proxy_ssl_server_name on;
        
        # CORS
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods *;
        add_header Access-Control-Allow-Headers *;
    }
}
```

#### 图片 CDN 代理

```nginx
server {
    listen 443 ssl;
    server_name bangumi-img.yourdomain.com;

    location / {
        proxy_pass https://lain.bgm.tv;
        proxy_set_header Host lain.bgm.tv;
        proxy_ssl_server_name on;
        
        # CORS
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods *;
        add_header Access-Control-Allow-Headers *;
    }
}
```

### Cloudflare Workers

#### API 代理

```javascript
addEventListener('fetch', event => {
  event.respondWith(handleRequest(event.request))
})

async function handleRequest(request) {
  const url = new URL(request.url)
  const targetUrl = 'https://api.bgm.tv' + url.pathname + url.search
  
  const response = await fetch(targetUrl, {
    method: request.method,
    headers: request.headers
  })
  
  const newResponse = new Response(response.body, response)
  newResponse.headers.set('Access-Control-Allow-Origin', '*')
  
  return newResponse
}
```

#### 图片 CDN 代理

```javascript
addEventListener('fetch', event => {
  event.respondWith(handleRequest(event.request))
})

async function handleRequest(request) {
  const url = new URL(request.url)
  const targetUrl = 'https://lain.bgm.tv' + url.pathname + url.search
  
  const response = await fetch(targetUrl, {
    method: request.method,
    headers: request.headers
  })
  
  const newResponse = new Response(response.body, response)
  newResponse.headers.set('Access-Control-Allow-Origin', '*')
  
  return newResponse
}
```

---

## 📝 相关文件

| 文件 | 说明 |
|------|------|
| `bangumi-config.sql` | 数据库配置 SQL |
| `test-bangumi-api.bat` | API 测试脚本 |
| `BANGUMI-SETUP.md` | 本文档 |

---

## ✅ 测试清单

```
□ 1. 执行 bangumi-config.sql
□ 2. 运行 test-bangumi-api.bat 测试镜像站
□ 3. 选择可用的 API 镜像站
□ 4. 选择可用的图片 CDN
□ 5. 在后台修改 bangumi_api_url 配置
□ 6. 在后台修改 bangumi_img_url 配置
□ 7. 重启后端服务
□ 8. 访问 http://localhost:8800/bangumi/test
□ 9. 访问追番页面测试
□ 10. 检查图片是否正常显示
```

---

## 🆘 需要帮助？

如果遇到问题，请提供：
1. 测试脚本的运行结果
2. 后端控制台的错误日志
3. 浏览器控制台的错误信息

我会帮你解决！
