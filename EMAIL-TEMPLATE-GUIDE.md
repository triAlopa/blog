# 邮件模板管理功能

## 功能说明

邮件模板使用 **Thymeleaf** 模板引擎，支持：
- 优先从数据库读取模板
- 数据库没有时自动从文件读取
- 支持 Thymeleaf 语法（更灵活）
- 后台管理界面编辑模板

## 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                    邮件模板渲染流程                           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   1. 调用 renderTemplate("verify_code", variables)          │
│                    ↓                                         │
│   2. 优先从数据库读取模板                                     │
│      - 有模板 → 使用数据库模板                                │
│      - 无模板 → 从文件读取                                    │
│                    ↓                                         │
│   3. 使用 Thymeleaf 渲染模板                                 │
│      - 支持 th:text, th:src 等语法                           │
│      - 支持条件判断、循环等高级功能                            │
│                    ↓                                         │
│   4. 返回渲染后的 HTML 内容                                   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## 文件结构

```
blog/
├── mojian-server/src/main/resources/
│   └── templates/email/
│       └── verify-code.html          # 默认模板文件（Thymeleaf语法）
├── mojian-commom/src/main/java/com/mojian/
│   ├── entity/SysEmailTemplate.java       # 模板实体类
│   ├── mapper/SysEmailTemplateMapper.java # 模板Mapper
│   ├── service/EmailTemplateService.java  # 模板服务接口
│   └── service/impl/EmailTemplateServiceImpl.java # 模板服务实现
└── mojian-admin/src/main/java/com/mojian/controller/tool/
    └── EmailTemplateController.java       # 模板管理控制器
```

## Thymeleaf 语法说明

### 变量输出

```html
<!-- 简单变量输出 -->
<span th:text="${siteName}">拾壹博客</span>

<!-- 图片src -->
<img th:src="${logoUrl}" />

<!-- 链接href -->
<a th:href="${siteUrl}">点击访问</a>
```

### 条件判断

```html
<!-- 如果有用户名 -->
<div th:if="${username}">
    <p>欢迎, <span th:text="${username}">用户</span></p>
</div>

<!-- 如果没有用户名 -->
<div th:unless="${username}">
    <p>欢迎访问</p>
</div>
```

### 循环

```html
<!-- 遍历列表 -->
<ul>
    <li th:each="item : ${items}" th:text="${item}">项目</li>
</ul>
```

### 字符串拼接

```html
<span th:text="'验证码是：' + ${code}">验证码是：12345</span>
```

## 模板变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `${code}` | 验证码 | `A1B2C` |
| `${siteName}` | 网站名称 | `拾壹博客` |
| `${siteUrl}` | 网站地址 | `https://www.shiyit.com` |
| `${logoUrl}` | Logo地址 | `https://xxx.com/logo.png` |
| `${username}` | 用户名 | `张三` |
| `${email}` | 邮箱地址 | `user@example.com` |

## 配置步骤

### 步骤1：执行数据库脚本

```bash
mysql -u root -p blog < email-template.sql
```

### 步骤2：重启后端服务

```bash
cd C:\Users\Chen\Desktop\shiyi-blog\blog
mvn spring-boot:run -pl mojian-server
```

### 步骤3：后台管理模板

1. 登录后台管理系统
2. 进入「系统工具」→「邮件模板」
3. 可以查看、编辑、添加邮件模板

## 优势

1. **优先级机制**：数据库 > 文件，灵活覆盖
2. **Thymeleaf 语法**：支持条件、循环、拼接等高级功能
3. **解耦**：模板与代码完全分离
4. **可管理**：通过后台界面管理模板
5. **可扩展**：支持添加多种邮件模板

## 添加新模板

### 方式1：通过后台管理界面

1. 进入「系统工具」→「邮件模板」
2. 点击「新增」
3. 填写模板信息
4. 使用 Thymeleaf 语法编写模板内容

### 方式2：通过数据库

```sql
INSERT INTO sys_email_template (template_code, template_name, subject, content, remark, status) 
VALUES ('register_notify', '注册通知', '欢迎注册 [[${siteName}]]', 
'<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <div style="padding: 20px;">
        <h1>欢迎注册</h1>
        <p>尊敬的 <span th:text="${username}">用户</span>，您好！</p>
        <p>感谢您注册 <span th:text="${siteName}">拾壹博客</span></p>
    </div>
</body>
</html>', 
'用于用户注册成功通知', 1);
```

### 方式3：通过文件

在 `src/main/resources/templates/email/` 目录下创建 `register_notify.html`：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <div style="padding: 20px;">
        <h1>欢迎注册</h1>
        <p>尊敬的 <span th:text="${username}">用户</span>，您好！</p>
        <p>感谢您注册 <span th:text="${siteName}">拾壹博客</span></p>
    </div>
</body>
</html>
```

## 代码使用示例

```java
@Autowired
private EmailTemplateService emailTemplateService;

public void sendEmail(String email) {
    // 准备变量
    Map<String, String> variables = new HashMap<>();
    variables.put("code", "12345");
    variables.put("siteName", "拾壹博客");
    variables.put("siteUrl", "https://www.shiyit.com");
    variables.put("logoUrl", "https://xxx.com/logo.png");
    
    // 渲染模板（优先从数据库，其次从文件）
    String content = emailTemplateService.renderTemplate("verify_code", variables);
    
    // 发送邮件
    sendEmail(email, "验证码", content);
}
```
