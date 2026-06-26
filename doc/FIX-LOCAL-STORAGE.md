# 本地文件存储修复指南

## 🎯 目标

1. 修改本地存储路径为：`D:\Chen\Pictures\dev_temp`
2. 解决访问本地文件提示登录的问题

---

## 📋 步骤1：更新数据库配置

### 方法1：使用SQL文件（推荐）

```bash
# 连接MySQL
mysql -u root -p

# 执行SQL文件
USE blog;
SOURCE C:/Users/Chen/Desktop/shiyi-blog/fix-storage-path.sql;
```

### 方法2：直接执行SQL

```sql
USE blog;

-- 更新本地存储路径
UPDATE sys_file_oss SET 
  storage_path = 'D:/Chen/Pictures/dev_temp/',
  domain = 'http://127.0.0.1:8800/localFile/',
  path_patterns = 'localFile/**',
  base_path = 'local-plus/',
  is_enable = 1,
  enable_access = 1
WHERE platform = 'local';

-- 验证配置
SELECT * FROM sys_file_oss WHERE platform = 'local';
```

---

## 📋 步骤2：创建存储目录

```bash
# Windows（已自动创建）
mkdir D:\Chen\Pictures\dev_temp

# 或者手动在文件资源管理器中创建
```

---

## 📋 步骤3：重启后端服务

**重要**：修改数据库配置后，必须重启后端服务才能生效！

```bash
# 1. 停止当前运行的后端服务（Ctrl+C）

# 2. 重新启动
cd C:\Users\Chen\Desktop\shiyi-blog\blog
mvn spring-boot:run -pl mojian-server

# 等待看到：系统启动成功！
```

---

## 🔍 步骤4：验证配置

### 4.1 验证服务启动

```bash
# 浏览器访问API文档（应该能正常打开）
http://127.0.0.1:8800/shiyi/doc.html
```

### 4.2 验证本地文件访问

**测试1：直接访问一个不存在的文件**
```
http://127.0.0.1:8800/localFile/test.jpg
```

**预期结果**：
- ✅ 返回404（文件不存在）= 配置正确
- ❌ 返回401（需要登录）= Sa-Token配置有问题
- ❌ 返回连接拒绝 = 服务没启动

**测试2：上传一个测试文件**

1. 打开浏览器，访问后台管理：http://localhost:3000
2. 登录后台（admin/123456）
3. 进入 **系统管理 → 文件管理**
4. 点击上传，选择一张图片
5. 查看返回的URL是否以 `http://127.0.0.1:8800/localFile/` 开头

### 4.3 验证文件是否保存到新目录

```bash
# 检查目录中是否有文件
ls -la D:\Chen\Pictures\dev_temp\local-plus\
```

---

## ⚠️ 如果仍然提示登录

### 可能原因1：Sa-Token配置未生效

检查 `SaTokenConfigure.java` 是否包含 `/localFile/**`：

```java
.excludePathPatterns(
    "/auth/login",
    "/localFile/**"  // ← 这一行必须存在
)
```

### 可能原因2：路径大小写问题

数据库中的 `path_patterns` 必须是 `localFile/**`（小写l，大写F）

### 可能原因3：Spring MVC资源映射问题

检查 `WebMvcConfig.java` 中的资源映射：

```java
// 这段代码从数据库读取配置并注册资源处理器
SysFileOss sysFileOss = sysFileOssMapper.selectOne(
    new LambdaQueryWrapper<SysFileOss>()
        .eq(SysFileOss::getPlatform, FileOssEnum.LOCAL.getValue())
);

if (sysFileOss != null) {
    registry.addResourceHandler(sysFileOss.getPathPatterns())  // "localFile/**"
            .addResourceLocations("file:" + sysFileOss.getStoragePath());  // "file:D:/Chen/Pictures/dev_temp/"
}
```

### 可能原因4：数据库配置未更新

确认数据库中的配置已经更新：

```sql
SELECT platform, storage_path, path_patterns, is_enable, enable_access 
FROM sys_file_oss 
WHERE platform = 'local';
```

应该看到：
| platform | storage_path | path_patterns | is_enable | enable_access |
|----------|--------------|---------------|-----------|---------------|
| local | D:/Chen/Pictures/dev_temp/ | localFile/** | 1 | 1 |

---

## 🧪 完整测试流程

### 1. 启动服务后，打开浏览器访问：

```
http://127.0.0.1:8800/localFile/test.jpg
```

- 如果返回 **404 Not Found** ✅ → 资源映射配置正确
- 如果返回 **401 Unauthorized** ❌ → Sa-Token拦截了请求

### 2. 如果返回401，检查控制台日志

查看后端控制台是否有类似日志：
```
[Sa-Token] xxx - 拦截请求: /localFile/test.jpg
```

如果有，说明Sa-Token配置未生效，需要检查：
- SaTokenConfigure.java 是否在正确的包路径下
- 是否有其他拦截器覆盖了配置

### 3. 临时解决方案（如果问题持续）

如果Sa-Token配置确实有问题，可以临时在 `application-dev.yml` 中添加：

```yaml
sa-token:
  exclude-path:
    - /localFile/**
```

但这种方式不推荐，应该优先检查 Java 配置类。

---

## 📊 配置文件路径总结

| 配置项 | 文件位置 |
|--------|----------|
| 存储路径配置 | 数据库 `sys_file_oss` 表 |
| Sa-Token排除路径 | `blog/mojian-auth/.../SaTokenConfigure.java` |
| 资源映射配置 | `blog/mojian-commom/.../WebMvcConfig.java` |
| 存储初始化 | `blog/mojian-file/.../FileStorageInit.java` |

---

## ✅ 成功标志

当一切配置正确后：

1. ✅ 访问 `http://127.0.0.1:8800/localFile/test.jpg` 返回 404（不是401）
2. ✅ 上传文件后，文件保存到 `D:\Chen\Pictures\dev_temp\local-plus\`
3. ✅ 返回的URL格式：`http://127.0.0.1:8800/localFile/local-plus/20250122/xxx.jpg`
4. ✅ 在浏览器中能直接访问上传的图片

---

## 🆘 如果还是不行

请提供以下信息：

1. 访问 `http://127.0.0.1:8800/localFile/test.jpg` 的具体错误信息
2. 后端控制台的错误日志
3. 执行 `SELECT * FROM sys_file_oss WHERE platform='local';` 的结果

我会帮你进一步排查！
