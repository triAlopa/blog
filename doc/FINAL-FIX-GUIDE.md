# 最终修复指南：本地文件访问401错误

## 🎯 问题
访问 `http://127.0.0.1:8800/localFile/test.jpg` 返回401 Unauthorized

## 🔍 根本原因
Spring MVC 的资源处理器没有正确配置，导致请求被 Sa-Token 拦截器处理

---

## ✅ 修复步骤（按顺序执行）

### 步骤1：更新数据库配置

```bash
# 执行SQL文件
mysql -u root -p blog < fix-local-access.sql
```

或者手动执行：
```sql
USE blog;

UPDATE sys_file_oss SET
  storage_path = 'D:/Chen/Pictures/dev_temp/',
  domain = 'http://127.0.0.1:8800/localFile/',
  path_patterns = 'localFile/**',
  base_path = 'local-plus/',
  is_enable = 1,
  enable_access = 1
WHERE platform = 'local';
```

---

### 步骤2：修改 WebMvcConfig.java（关键！）

**文件位置**：`blog/mojian-commom/src/main/java/com/mojian/config/WebMvcConfig.java`

**替换 `addResourceHandlers` 方法为以下代码**：

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Knife4j 文档资源
    registry.addResourceHandler("doc.html")
            .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");

    // 本地存储文件访问
    // 方案1：从数据库读取配置（推荐）
    try {
        SysFileOss sysFileOss = sysFileOssMapper.selectOne(
                new LambdaQueryWrapper<SysFileOss>()
                        .eq(SysFileOss::getPlatform, FileOssEnum.LOCAL.getValue())
        );

        if (sysFileOss != null && sysFileOss.getPathPatterns() != null && sysFileOss.getStoragePath() != null) {
            System.out.println("========================================");
            System.out.println("注册本地文件资源处理器:");
            System.out.println("  路径模式: " + sysFileOss.getPathPatterns());
            System.out.println("  存储位置: file:" + sysFileOss.getStoragePath());
            System.out.println("========================================");

            registry.addResourceHandler(sysFileOss.getPathPatterns())
                    .addResourceLocations("file:" + sysFileOss.getStoragePath());
        } else {
            System.out.println("警告: 未找到本地存储配置，使用默认配置");
            // 方案2：硬编码默认配置（如果数据库配置为空）
            registry.addResourceHandler("localFile/**")
                    .addResourceLocations("file:D:/Chen/Pictures/dev_temp/");
        }
    } catch (Exception e) {
        System.out.println("错误: 读取本地存储配置失败: " + e.getMessage());
        e.printStackTrace();
        // 出错时使用默认配置
        registry.addResourceHandler("localFile/**")
                .addResourceLocations("file:D:/Chen/Pictures/dev_temp/");
    }
}
```

**完整的 WebMvcConfig.java 文件已创建为**：`fix-webmvc-config.java`

**替换方法**：
```bash
# 备份原文件
cp blog/mojian-commom/src/main/java/com/mojian/config/WebMvcConfig.java blog/mojian-commom/src/main/java/com/mojian/config/WebMvcConfig.java.bak

# 复制修复后的文件
cp fix-webmvc-config.java blog/mojian-commom/src/main/java/com/mojian/config/WebMvcConfig.java
```

---

### 步骤3：确认 SaTokenConfigure.java 配置正确

**文件位置**：`blog/mojian-auth/src/main/java/com/mojian/config/satoken/SaTokenConfigure.java`

**确认包含以下配置**：
```java
.excludePathPatterns(
    "/auth/login",
    "/auth/logout",
    "/auth/verify",
    "/swagger-ui/**",
    "/webjars/**",
    "/v3/api-docs/**",
    "/doc.html",
    "/favicon.ico",
    "/swagger-resources",
    "/api/**",
    "/wechat/**",
    "/localFile/**"      // ← 确保这行存在
)
```

---

### 步骤4：创建存储目录

```bash
# 确保目录存在
mkdir -p D:\Chen\Pictures\dev_temp

# 验证目录
ls -la D:\Chen\Pictures\dev_temp
```

---

### 步骤5：清理并重新编译

```bash
cd C:\Users\Chen\Desktop\shiyi-blog\blog

# 清理旧的编译文件
mvn clean

# 重新编译
mvn compile
```

---

### 步骤6：重启后端服务

```bash
# 启动服务
mvn spring-boot:run -pl mojian-server

# 等待看到：系统启动成功！
```

**观察启动日志**，应该看到：
```
========================================
注册本地文件资源处理器:
  路径模式: localFile/**
  存储位置: file:D:/Chen/Pictures/dev_temp/
========================================
```

---

### 步骤7：验证修复

**测试1**：访问本地文件
```bash
# 浏览器访问
http://127.0.0.1:8800/localFile/test.jpg

# 预期结果：404 Not Found ✅
# 错误结果：401 Unauthorized ❌
```

**测试2**：上传文件测试
1. 访问后台管理：http://localhost:3000
2. 登录：admin / 123456
3. 进入 **系统管理 → 文件管理**
4. 上传一张图片
5. 检查返回的URL是否以 `http://127.0.0.1:8800/localFile/` 开头

**测试3**：验证文件保存位置
```bash
# 检查文件是否保存到新目录
ls -la D:\Chen\Pictures\dev_temp\local-plus\
```

---

## 🔧 快速修复脚本

已创建以下文件：

1. **`fix-local-access.sql`** - 数据库配置SQL
2. **`fix-webmvc-config.java`** - 修复后的 WebMvcConfig.java
3. **`FIX-401-ERROR.md`** - 详细修复指南
4. **`quick-fix-401.bat`** - 快速修复脚本

---

## ⚠️ 如果还是401

### 检查清单：

```
□ 数据库配置已更新（SELECT * FROM sys_file_oss WHERE platform='local'）
□ WebMvcConfig.java 已修改并重新编译
□ SaTokenConfigure.java 包含 /localFile/** 排除路径
□ 存储目录已创建（D:\Chen\Pictures\dev_temp）
□ 后端服务已重启
□ 启动日志显示资源处理器注册成功
```

### 调试方法：

1. **查看启动日志**：
   - 确认看到 "注册本地文件资源处理器" 日志
   - 确认路径模式和存储位置正确

2. **测试其他排除路径**：
   ```bash
   # 测试 Sa-Token 排除的其他路径
   curl http://127.0.0.1:8800/auth/login  # 应该返回非401
   curl http://127.0.0.1:8800/doc.html     # 应该返回文档页面
   ```

3. **检查数据库配置**：
   ```sql
   SELECT platform, storage_path, path_patterns, is_enable, enable_access
   FROM sys_file_oss
   WHERE platform = 'local';
   ```

---

## 📋 配置对照表

| 配置项 | 正确值 | 说明 |
|--------|--------|------|
| `platform` | `local` | 本地存储标识 |
| `storage_path` | `D:/Chen/Pictures/dev_temp/` | 本地存储路径（正斜杠，结尾有斜杠） |
| `domain` | `http://127.0.0.1:8800/localFile/` | 访问域名 |
| `path_patterns` | `localFile/**` | Spring MVC资源映射模式 |
| `base_path` | `local-plus/` | 存储基础路径 |
| `is_enable` | `1` | 启用存储 |
| `enable_access` | `1` | 启用访问 |

---

## 🎉 成功标志

当一切配置正确后：

1. ✅ 访问 `http://127.0.0.1:8800/localFile/test.jpg` 返回 404（不是401）
2. ✅ 启动日志显示 "注册本地文件资源处理器"
3. ✅ 上传文件后，文件保存到 `D:\Chen\Pictures\dev_temp\local-plus\`
4. ✅ 返回的URL格式：`http://127.0.0.1:8800/localFile/local-plus/20250122/xxx.jpg`

---

## 🆘 需要帮助？

如果按照以上步骤操作后仍然返回401，请提供：

1. **后端启动日志**（特别是资源处理器相关的日志）
2. **数据库查询结果**：`SELECT * FROM sys_file_oss WHERE platform='local';`
3. **是否修改了 WebMvcConfig.java**
4. **访问的具体URL和返回的完整错误信息**

我会帮你进一步排查！
