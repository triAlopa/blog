# 拾壹博客部署指南

本文档详细介绍拾壹博客系统的部署流程、中间件配置以及图片存储方案。

---

## 目录

- [系统架构](#系统架构)
- [环境要求](#环境要求)
- [中间件部署](#中间件部署)
  - [MySQL](#1-mysql)
  - [Redis](#2-redis)
  - [Elasticsearch（可选）](#3-elasticsearch可选)
- [后端部署](#后端部署)
- [前端部署](#前端部署)
- [图片存储配置](#图片存储配置)
  - [本地存储](#方案一本地存储)
  - [七牛云OSS](#方案二七牛云oss)
  - [阿里云OSS](#方案三阿里云oss)
  - [腾讯云COS](#方案四腾讯云cos)
  - [MinIO](#方案五minio)
- [生产环境配置](#生产环境配置)
- [常见问题](#常见问题)

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        Nginx (反向代理)                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  blog-web    │  │  blog-admin  │  │  uniapp-blog │          │
│  │  (Vue 2)     │  │  (Vue 3)     │  │  (UniApp)    │          │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘          │
│         │                 │                                      │
│         └────────┬────────┘                                      │
│                  │                                               │
│         ┌────────▼────────┐                                      │
│         │   Spring Boot   │                                      │
│         │   (port 8800)   │                                      │
│         └────────┬────────┘                                      │
│                  │                                               │
│    ┌─────────────┼─────────────┬─────────────┐                  │
│    │             │             │             │                  │
│    ▼             ▼             ▼             ▼                  │
│ ┌──────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│ │MySQL │  │  Redis   │  │    ES    │  │ 文件存储  │            │
│ │5.5+  │  │  6.0+    │  │  7.9.2  │  │(本地/云)  │            │
│ └──────┘  └──────────┘  └──────────┘  └──────────┘            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 1.8+ | 推荐 OpenJDK 8 |
| Maven | 3.6+ | 构建工具 |
| Node.js | 16+ | 前端构建 |
| MySQL | 5.5+ | 必需 |
| Redis | 6.0+ | 必需，用于缓存和Session |
| Elasticsearch | 7.9.2 | 可选，可用于全文搜索 |
| Nginx | 1.20+ | 生产环境反向代理 |

---

## 中间件部署

### 1. MySQL

#### 安装

```bash
# CentOS/RHEL
yum install mysql-server
systemctl start mysqld
systemctl enable mysqld

# Ubuntu/Debian
apt-get install mysql-server
systemctl start mysql
systemctl enable mysql

# Docker (推荐)
docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=your_password \
  -e MYSQL_DATABASE=blog \
  -v /data/mysql:/var/lib/mysql \
  mysql:5.7
```

#### 初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 导入SQL文件（在项目根目录）
mysql -u root -p blog < mj-blog.sql
```

#### 配置连接

编辑 `blog/mojian-server/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/blog?characterEncoding=UTF-8&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai&tinyInt1isBit=false&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password  # 修改为你的密码
```

---

### 2. Redis

#### 安装

```bash
# CentOS/RHEL
yum install redis
systemctl start redis
systemctl enable redis

# Ubuntu/Debian
apt-get install redis-server
systemctl start redis-server
systemctl enable redis-server

# Docker (推荐)
docker run -d \
  --name redis \
  -p 6379:6379 \
  -v /data/redis:/data \
  redis:6.0 --requirepass "your_password"
```

#### 配置连接

编辑 `blog/mojian-server/src/main/resources/application-dev.yml`：

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    timeout: 3000
    database: 8          # 使用第8号数据库
    password: your_password  # 如果设置了密码
```

#### Redis用途

- Sa-Token 会话存储
- 接口访问限流（`@AccessLimit` 注解）
- 缓存热点数据
- 验证码存储

---

### 3. Elasticsearch（可选）

Elasticsearch 用于文章全文搜索，**可以不安装**，系统会自动降级使用 MySQL 的 LIKE 查询。

#### 安装

```bash
# Docker (推荐)
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -v /data/elasticsearch:/usr/share/elasticsearch/data \
  elasticsearch:7.9.2
```

#### 配置

项目支持两种搜索模式，通过配置切换：

- **Elasticsearch 模式**：全文搜索，支持高亮分词
- **MySQL 模式**：使用 LIKE 查询，适合小数据量

---

## 后端部署

### 1. 修改配置

编辑 `blog/mojian-server/src/main/resources/application-dev.yml`：

```yaml
server:
  port: 8800  # 服务端口

spring:
  datasource:
    # MySQL连接配置
    url: jdbc:mysql://your_host:3306/blog?...
    username: your_username
    password: your_password

  redis:
    # Redis连接配置
    host: your_redis_host
    port: 6379
    password: your_redis_password
    database: 8

# Knife4j API文档配置
knife4j:
  enable: true
  basic:
    enable: true
    username: test      # 文档访问用户名
    password: 123456    # 文档访问密码

# 第三方登录配置（按需配置）
qq:
  app-id: your_qq_app_id
  app-secret: your_qq_app_secret

weibo:
  app-id: your_weibo_app_id
  app-secret: your_weibo_app_secret

gitee:
  app-id: your_gitee_app_id
  app-secret: your_gitee_app_secret

github:
  app-id: your_github_app_id
  app-secret: your_github_app_secret
```

### 2. 构建打包

```bash
# 进入后端目录
cd blog

# 清理并打包（跳过测试）
mvn clean package -DskipTests

# 打包完成后，jar文件位于 blog/mojian-server/target/mojian-blog.jar
```

### 3. 启动服务

```bash
# 直接运行
java -jar blog/mojian-server/target/mojian-blog.jar

# 指定运行环境
java -jar blog/mojian-server/target/mojian-blog.jar --spring.profiles.active=dev

# 后台运行
nohup java -jar blog/mojian-server/target/mojian-blog.jar > blog.log 2>&1 &

# 指定JVM参数
java -Xms512m -Xmx1024m -jar blog/mojian-server/target/mojian-blog.jar
```

### 4. 验证启动

- 服务地址：http://localhost:8800
- API文档：http://localhost:8800/shiyi/doc.html
  - 用户名：test
  - 密码：123456

---

## 前端部署

### 开发环境

#### blog-web（前台）

```bash
# 进入目录
cd blog-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
# 访问 http://localhost:3000
```

#### blog-admin（后台）

```bash
# 进入目录
cd blog-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
# 访问 http://localhost:3000
```

### 生产环境构建

```bash
# 构建 blog-web
cd blog-web
npm run build
# 产出目录：blog-web/dist/

# 构建 blog-admin
cd blog-admin
npm run build
# 产出目录：blog-admin/dist/
```

### Nginx 配置

```nginx
# 前台博客
server {
    listen 80;
    server_name www.yourdomain.com;

    root /var/www/blog-web;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /moian/ {
        proxy_pass http://localhost:8800/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket代理
    location /moian/websocket/ {
        proxy_pass http://localhost:8800/websocket/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}

# 后台管理
server {
    listen 80;
    server_name admin.yourdomain.com;

    root /var/www/blog-admin;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /moian/ {
        proxy_pass http://localhost:8800/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 图片存储配置

系统使用 **x-file-storage** 库，支持多种存储平台，可在后台管理界面动态切换。

### 存储平台枚举

| 平台标识 | 说明 |
|----------|------|
| `local` | 本地存储 |
| `qiniu` | 七牛云OSS |
| `ali` | 阿里云OSS |
| `tencent` | 腾讯云COS |
| `minio` | MinIO对象存储 |

### 存储配置表结构

系统通过 `sys_file_oss` 表动态管理存储配置：

```sql
CREATE TABLE `sys_file_oss` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) DEFAULT NULL COMMENT '访问域名',
  `access_key` varchar(255) DEFAULT NULL COMMENT 'access-key',
  `secret_key` varchar(255) DEFAULT NULL COMMENT 'secret-key',
  `bucket` varchar(255) DEFAULT NULL COMMENT '空间名',
  `base_path` varchar(255) DEFAULT NULL COMMENT '存储基础路径',
  `platform` varchar(50) DEFAULT NULL COMMENT '存储类型(local/qiniu/ali/tencent/minio)',
  `is_enable` int DEFAULT 0 COMMENT '是否启用(1启用,0禁用)',
  `storage_path` varchar(255) DEFAULT NULL COMMENT '本地存储路径',
  `enable_access` int DEFAULT 0 COMMENT '是否允许访问(本地存储用)',
  `path_patterns` varchar(255) DEFAULT NULL COMMENT '访问路径模式(本地存储用)',
  `region` varchar(100) DEFAULT NULL COMMENT '仓库所在地域(腾讯云用)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
);
```

---

### 方案一：本地存储

适合开发环境或小规模部署。

#### 配置步骤

1. **在后台管理界面配置**

   进入 **系统管理 → 存储管理**，添加本地存储配置：

   | 字段 | 示例值 | 说明 |
   |------|--------|------|
   | 存储类型 | `local` | 固定为 local |
   | 访问域名 | `http://yourdomain.com` | 图片访问域名 |
   | 存储路径 | `/data/upload/` | 服务器上的绝对路径 |
   | 访问路径 | `/localFile/**` | URL路径模式 |
   | 是否启用 | `是` | 启用此存储 |

2. **或直接操作数据库**

   ```sql
   INSERT INTO sys_file_oss (domain, platform, storage_path, path_patterns, is_enable, create_time)
   VALUES (
     'http://localhost:8800',
     'local',
     '/data/upload/',
     '/localFile/**',
     1,
     NOW()
   );
   ```

3. **创建存储目录**

   ```bash
   mkdir -p /data/upload
   chmod 755 /data/upload
   ```

#### 工作原理

- 图片上传时，保存到 `storage_path` 指定的目录
- 文件路径格式：`/data/upload/20250122/avatar/12_abc.jpg`
- 访问URL：`http://yourdomain.com/localFile/20250122/avatar/12_abc.jpg`
- Spring MVC 通过 `WebMvcConfig` 配置的资源映射来提供文件访问

---

### 方案二：七牛云OSS

适合国内生产环境，提供CDN加速。

#### 准备工作

1. 注册 [七牛云](https://www.qiniu.com/) 账号
2. 创建存储空间（Bucket）
3. 获取 AccessKey 和 SecretKey
4. 配置域名（已备案）

#### 配置步骤

在后台管理界面 **系统管理 → 存储管理** 添加配置：

| 字段 | 示例值 | 说明 |
|------|--------|------|
| 存储类型 | `qiniu` | 固定为 qiniu |
| AccessKey | `your_access_key` | 七牛云AK |
| SecretKey | `your_secret_key` | 七牛云SK |
| 空间名 | `my-blog` | Bucket名称 |
| 访问域名 | `https://cdn.yourdomain.com` | 绑定的域名 |
| 存储路径 | `blog/` | Bucket内路径前缀 |
| 是否启用 | `是` | 启用此存储 |

---

### 方案三：阿里云OSS

#### 准备工作

1. 开通 [阿里云OSS](https://www.aliyun.com/product/oss) 服务
2. 创建 Bucket
3. 获取 AccessKey

#### 配置步骤

| 字段 | 示例值 | 说明 |
|------|--------|------|
| 存储类型 | `ali` | 固定为 ali |
| AccessKey | `your_access_key` | 阿里云AK |
| SecretKey | `your_secret_key` | 阿里云SK |
| 空间名 | `my-blog` | Bucket名称 |
| 访问域名 | `https://oss-cn-hangzhou.aliyuncs.com` | Endpoint |
| 存储路径 | `blog/` | 存储路径前缀 |
| 是否启用 | `是` | 启用此存储 |

---

### 方案四：腾讯云COS

#### 准备工作

1. 开通 [腾讯云COS](https://cloud.tencent.com/product/cos) 服务
2. 创建 Bucket
3. 获取 SecretId 和 SecretKey

#### 配置步骤

| 字段 | 示例值 | 说明 |
|------|--------|------|
| 存储类型 | `tencent` | 固定为 tencent |
| AccessKey | `your_secret_id` | 腾讯云 SecretId |
| SecretKey | `your_secret_key` | 腾讯云 SecretKey |
| 空间名 | `my-blog-1250000000` | Bucket名称 |
| 访问域名 | `https://cos.ap-guangzhou.myqcloud.com` | 访问域名 |
| 存储路径 | `blog/` | 存储路径前缀 |
| 地域 | `ap-guangzhou` | Bucket所在地域 |
| 是否启用 | `是` | 启用此存储 |

---

### 方案五：MinIO

适合私有化部署，兼容S3协议。

#### 安装MinIO

```bash
# Docker
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=admin" \
  -e "MINIO_ROOT_PASSWORD=admin123456" \
  -v /data/minio:/data \
  minio/minio server /data --console-address ":9001"
```

#### 配置步骤

| 字段 | 示例值 | 说明 |
|------|--------|------|
| 存储类型 | `minio` | 固定为 minio |
| AccessKey | `admin` | MinIO用户名 |
| SecretKey | `admin123456` | MinIO密码 |
| 空间名 | `blog` | Bucket名称 |
| 访问域名 | `http://your-server:9000` | MinIO地址 |
| 存储路径 | `images/` | 存储路径前缀 |
| 是否启用 | `是` | 启用此存储 |

---

### 图片上传流程

```
┌──────────┐     ┌──────────┐     ┌──────────────┐     ┌──────────┐
│  前端     │────▶│  后端     │────▶│ x-file-storage│────▶│ 存储平台  │
│ 上传文件  │     │ FileCtrl │     │   库处理     │     │ 保存文件  │
└──────────┘     └──────────┘     └──────────────┘     └──────────┘
                        │                                      │
                        ▼                                      │
                 ┌──────────────┐                              │
                 │ sys_file_oss │                              │
                 │   记录文件   │◀─────────────────────────────┘
                 └──────────────┘         返回URL
```

#### 上传接口

- **URL**: `POST /file/upload`
- **参数**:
  - `file`: 文件（MultipartFile）
  - `source`: 来源标识（可选，如 `avatar`、`article`）
- **返回**: 文件访问URL

#### 前端调用示例

```javascript
// Vue组件中
const formData = new FormData();
formData.append('file', file);
formData.append('source', 'article');

axios.post('/api/file/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
}).then(res => {
  console.log('图片URL:', res.data.data);
});
```

#### 存储路径规则

系统自动按日期和来源生成路径：

```
/{basePath}/{日期}/{来源}/{随机数}_{原文件名}

示例：
/blog/20250122/article/12_myphoto.jpg
/blog/20250122/avatar/45_user.png
```

---

### 动态切换存储平台

系统支持运行时切换存储平台，无需重启：

1. 进入后台 **系统管理 → 存储管理**
2. 点击要启用的存储平台
3. 点击"启用"按钮
4. 系统自动更新 `FileStorageService` 的默认平台

代码实现（`FileStorageInit.java`）：

```java
@PostConstruct
private void init() {
    // 启动时从数据库加载所有存储配置
    List<SysFileOss> sysFileOssList = sysFileOssMapper.selectList(null);

    for (SysFileOss sysFileOss : sysFileOssList) {
        // 根据platform类型初始化对应的存储客户端
        if (sysFileOss.getPlatform().equals(FileOssEnum.QINIU.getValue())) {
            // 初始化七牛云客户端
        } else if (sysFileOss.getPlatform().equals(FileOssEnum.ALI.getValue())) {
            // 初始化阿里云客户端
        }
        // ... 其他平台

        // 设置启用的平台为默认
        if (sysFileOss.getIsEnable() == 1) {
            service.getProperties().setDefaultPlatform(sysFileOss.getPlatform());
        }
    }
}
```

---

## 生产环境配置

### 推荐服务器配置

| 配置项 | 最低配置 | 推荐配置 |
|--------|----------|----------|
| CPU | 1核 | 2核 |
| 内存 | 2GB | 4GB |
| 硬盘 | 40GB | 100GB SSD |
| 带宽 | 1Mbps | 5Mbps |

### 关闭Elasticsearch

如果服务器配置较低，可以不安装ES，系统会自动使用MySQL搜索：

```yaml
# 在 application-dev.yml 中不配置ES连接即可
# 或者通过配置开关禁用
```

### JVM参数优化

```bash
# 2核4G服务器推荐配置
java -Xms1g -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar mojian-blog.jar
```

### Docker部署

```yaml
# docker-compose.yml
version: '3'

services:
  mysql:
    image: mysql:5.7
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: blog
    volumes:
      - ./data/mysql:/var/lib/mysql
      - ./mj-blog.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:6.0
    ports:
      - "6379:6379"
    command: redis-server --requirepass your_password
    volumes:
      - ./data/redis:/data

  blog:
    image: java:8
    ports:
      - "8800:8800"
    volumes:
      - ./blog/mojian-server/target/mojian-blog.jar:/app.jar
      - ./data/upload:/data/upload
    command: java -jar /app.jar
    depends_on:
      - mysql
      - redis

  nginx:
    image: nginx:1.20
    ports:
      - "80:80"
    volumes:
      - ./blog-web/dist:/var/www/blog-web
      - ./blog-admin/dist:/var/www/blog-admin
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
    depends_on:
      - blog
```

---

## 常见问题

### 1. 启动报错：Redis连接失败

**原因**：Redis未启动或密码配置错误

**解决**：
```bash
# 检查Redis状态
systemctl status redis

# 测试连接
redis-cli -h 127.0.0.1 -p 6379 -a your_password ping
```

### 2. 图片上传失败

**原因**：存储目录权限不足或配置错误

**解决**：
```bash
# 本地存储时，确保目录存在且有写入权限
mkdir -p /data/upload
chmod 755 /data/upload
chown -R www:www /data/upload  # 如果使用Nginx
```

### 3. 前端无法访问API

**原因**：跨域或代理配置错误

**解决**：
- 开发环境：检查 `.env.development` 中的 `VITE_APP_API_URL`
- 生产环境：检查Nginx的 `proxy_pass` 配置

### 4. WebSocket连接失败

**原因**：Nginx未配置WebSocket代理

**解决**：
```nginx
location /moian/websocket/ {
    proxy_pass http://localhost:8800/websocket/;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

### 5. 数据库导入失败

**原因**：字符集不匹配

**解决**：
```sql
-- 确保数据库使用utf8mb4
ALTER DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

---

## 推荐部署流程

### 快速开始（开发环境）

```bash
# 1. 启动MySQL和Redis
docker-compose up -d mysql redis

# 2. 导入数据库
mysql -u root -p blog < mj-blog.sql

# 3. 启动后端
cd blog
mvn spring-boot:run -pl mojian-server

# 4. 启动前端
cd blog-web && npm install && npm run dev

# 5. 访问
# 前台: http://localhost:3000
# 后台: http://localhost:3000 (需配置路由)
# API文档: http://localhost:8800/shiyi/doc.html
```

### 生产环境部署

```bash
# 1. 部署中间件
docker-compose up -d

# 2. 构建后端
cd blog && mvn clean package -DskipTests

# 3. 构建前端
cd blog-web && npm run build
cd blog-admin && npm run build

# 4. 配置Nginx
cp nginx.conf /etc/nginx/conf.d/
nginx -s reload

# 5. 启动后端服务
nohup java -jar blog/mojian-server/target/mojian-blog.jar > blog.log 2>&1 &

# 6. 访问测试
curl http://localhost:8800/shiyi/doc.html
```

---

## 默认账号

| 系统 | 用户名 | 密码 |
|------|--------|------|
| 博客后台 | admin | 123456 |
| API文档 | test | 123456 |

---

## 技术支持

- 项目地址：[https://gitee.com/quequnlong/shiyi-blog](https://gitee.com/quequnlong/shiyi-blog)
- 在线演示：[https://www.shiyit.site](https://www.shiyit.site)
- 后台演示：[https://admin.shiyit.site](https://admin.shiyit.site)
