# 修复本地文件访问401错误

## 问题分析

访问 `http://127.0.0.1:8800/localFile/test.jpg` 返回401，说明 Sa-Token 拦截了请求。

**根本原因**：
- `SaTokenConfigure.java` 配置了排除路径 `/localFile/**`
- 但 Spring MVC 的资源处理器可能没有正确配置
- 或者配置的执行顺序有问题

---

## 🔧 修复方案

### 方案1：检查并更新数据库配置（优先执行）

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

### 方案2：修复 WebMvcConfig.java（关键修复）

**文件位置**：`blog/mojian-commom/src/main/java/com/mojian/config/WebMvcConfig.java`

**问题**：资源处理器可能没有正确从数据库读取配置

**修复步骤**：

1. 打开文件 `WebMvcConfig.java`
2. 找到 `addResourceHandlers` 方法
3. 确保代码正确读取数据库配置

**当前代码**（可能有问题）：
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // ... 其他配置

    SysFileOss sysFileOss = sysFileOssMapper.selectOne(
        new LambdaQueryWrapper<SysFileOss>()
            .eq(SysFileOss::getPlatform, FileOssEnum.LOCAL.getValue())
    );

    if (sysFileOss != null) {
        registry.addResourceHandler(sysFileOss.getPathPatterns())
                .addResourceLocations("file:" + sysFileOss.getStoragePath());
    }
}
```

**问题可能在于**：
1. `sysFileOssMapper` 注入失败
2. 数据库查询返回 null
3. 路径格式不正确

**建议修改为**（添加日志）：
```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // ... 其他配置

    try {
        SysFileOss sysFileOss = sysFileOssMapper.selectOne(
            new LambdaQueryWrapper<SysFileOss>()
                .eq(SysFileOss::getPlatform, FileOssEnum.LOCAL.getValue())
        );

        if (sysFileOss != null && sysFileOss.getPathPatterns() != null) {
            System.out.println("注册本地文件资源处理器:");
            System.out.println("  路径模式: " + sysFileOss.getPathPatterns());
            System.out.println("  存储位置: file:" + sysFileOss.getStoragePath());

            registry.addResourceHandler(sysFileOss.getPathPatterns())
                    .addResourceLocations("file:" + sysFileOss.getStoragePath());
        } else {
            System.out.println("警告: 未找到本地存储配置或配置为空");
        }
    } catch (Exception e) {
        System.out.println("错误: 注册本地文件资源处理器失败: " + e.getMessage());
        e.printStackTrace();
    }
}
```

---

### 方案3：临时修复 - 硬编码资源映射（如果方案2不生效）

**文件位置**：`blog/mojian-commom/src/main/java/com/mojian/config/WebMvcConfig.java`

**修改 `addResourceHandlers` 方法**：

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Knife4j 文档资源
    registry.addResourceHandler("doc.html")
            .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");

    // 方案A：从数据库读取配置（推荐）
    SysFileOss sysFileOss = sysFileOssMapper.selectOne(
        new LambdaQueryWrapper<SysFileOss>()
            .eq(SysFileOss::getPlatform, FileOssEnum.LOCAL.getValue())
    );

    if (sysFileOss != null) {
        registry.addResourceHandler(sysFileOss.getPathPatterns())
                .addResourceLocations("file:" + sysFileOss.getStoragePath());
    }

    // 方案B：硬编码配置（临时调试用，如果方案A不生效）
    // 取消下面的注释，注释掉上面的方案A
    /*
    registry.addResourceHandler("localFile/**")
            .addResourceLocations("file:D:/Chen/Pictures/dev_temp/");
    */
}
```

---

### 方案4：检查 Sa-Token 配置是否生效

**文件位置**：`blog/mojian-auth/src/main/java/com/mojian/config/satoken/SaTokenConfigure.java`

**确认配置正确**：
```java
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
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
                );
    }
}
```

**添加日志验证**（可选）：
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    System.out.println("注册 Sa-Token 拦截器...");
    System.out.println("排除路径: /localFile/**");

    registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
            .addPathPatterns("/**")
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
                    "/localFile/**"
            );
}
```

---

## 🚀 执行步骤

### 步骤1：更新数据库配置
```bash
mysql -u root -p blog < fix-local-access.sql
```

### 步骤2：检查代码修改
1. 确认 `WebMvcConfig.java` 中的资源处理器配置正确
2. 确认 `SaTokenConfigure.java` 中的排除路径配置正确

### 步骤3：重启后端服务
```bash
# 停止当前服务（Ctrl+C）
# 重新启动
cd C:\Users\Chen\Desktop\shiyi-blog\blog
mvn spring-boot:run -pl mojian-server
```

### 步骤4：查看启动日志
启动时应该看到类似日志：
```
注册本地文件资源处理器:
  路径模式: localFile/**
  存储位置: file:D:/Chen/Pictures/dev_temp/

注册 Sa-Token 拦截器...
排除路径: /localFile/**
```

### 步骤5：验证修复
```bash
# 访问本地文件
curl http://127.0.0.1:8800/localFile/test.jpg

# 预期结果：
# - 404 Not Found (文件不存在) ✅
# - 401 Unauthorized (仍然被拦截) ❌
```

---

## 🔍 调试方法

### 方法1：查看控制台日志
启动后端服务时，观察控制台输出，确认：
1. 资源处理器是否注册成功
2. Sa-Token 排除路径是否生效

### 方法2：使用浏览器开发者工具
1. 打开浏览器，按 F12 打开开发者工具
2. 访问 `http://127.0.0.1:8800/localFile/test.jpg`
3. 查看 Network 标签页的请求详情
4. 查看 Response Headers 和 Status Code

### 方法3：测试其他排除路径
```bash
# 测试 Sa-Token 排除的其他路径
curl http://127.0.0.1:8800/auth/login
# 应该返回非401响应

curl http://127.0.0.1:8800/doc.html
# 应该返回文档页面

curl http://127.0.0.1:8800/localFile/test.jpg
# 应该返回404（不是401）
```

---

## ⚠️ 常见问题

### 问题1：修改代码后不生效
**原因**：可能有缓存或没有重新编译

**解决**：
```bash
# 清理并重新编译
cd C:\Users\Chen\Desktop\shiyi-blog\blog
mvn clean compile

# 重新启动
mvn spring-boot:run -pl mojian-server
```

### 问题2：数据库配置未更新
**验证**：
```sql
SELECT * FROM sys_file_oss WHERE platform = 'local';
```

**解决**：重新执行 `fix-local-access.sql`

### 问题3：路径格式错误
**正确格式**：
- `storage_path`: `D:/Chen/Pictures/dev_temp/` (正斜杠，结尾有斜杠)
- `path_patterns`: `localFile/**` (不以斜杠开头)
- `domain`: `http://127.0.0.1:8800/localFile/` (结尾有斜杠)

---

## 📋 快速检查清单

```
□ 1. 数据库配置已更新（storage_path = 'D:/Chen/Pictures/dev_temp/'）
□ 2. 存储目录已创建（D:\Chen\Pictures\dev_temp）
□ 3. WebMvcConfig.java 资源处理器配置正确
□ 4. SaTokenConfigure.java 排除路径配置正确
□ 5. 后端服务已重启
□ 6. 访问 http://127.0.0.1:8800/localFile/test.jpg 返回404（不是401）
```

---

## 🆘 如果还是401

请提供以下信息：

1. **后端启动日志**（特别是资源处理器和 Sa-Token 相关的日志）
2. **数据库配置**：`SELECT * FROM sys_file_oss WHERE platform='local';`
3. **代码修改**：是否修改了 `WebMvcConfig.java` 或 `SaTokenConfigure.java`
4. **访问URL**：完整的访问地址

我会帮你进一步排查！
