# 登录问题修复指南

## 问题分析
登录失败可能的原因：
1. 密码错误或密码格式不正确
2. 用户被禁用（status=0）
3. 用户不存在

## 解决方案

### 方案1：重置 admin 密码（推荐）

```bash
mysql -u root -p blog < reset-admin-password.sql
```

然后使用 `admin / 123456` 登录

---

### 方案2：添加新测试用户

```bash
mysql -u root -p blog < quick-add-user.sql
```

默认添加用户 `chen / 123456`

**修改用户名和密码**：
编辑 `quick-add-user.sql`，修改这两行：
```sql
SET @my_username = 'chen';           -- 改成你的用户名
SET @my_password = '123456';         -- 改成你的密码
```

---

### 方案3：自定义密码

#### 步骤1：生成 BCrypt 密码

运行密码生成工具：
```bash
cd C:\Users\Chen\Desktop\shiyi-blog\blog
mvn compile exec:java -Dexec.mainClass="com.mojian.utils.PasswordGenerator"
```

或者手动修改 `PasswordGenerator.java` 中的密码，然后运行。

#### 步骤2：使用生成的密码

```sql
INSERT INTO sys_user (username, password, nickname, status, create_time, update_time)
VALUES ('你的用户名', '生成的BCrypt密码', '你的昵称', 1, NOW(), NOW());

INSERT INTO sys_user_role (user_id, role_id) VALUES (LAST_INSERT_ID(), 2);
```

---

## 常用密码 BCrypt 值

| 密码 | BCrypt 值 |
|------|-----------|
| 123456 | `$2a$10$GSHv.XwqBkizplz5j2pcmu73IRY2rgtxCYQwAvSXMvu9SryzydLpe` |
| admin123 | `$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfRGBB6Qsyyp4I1xBMDZi` |

---

## 验证用户状态

```sql
-- 查看所有用户
SELECT id, username, nickname, status FROM sys_user;

-- 查看用户角色
SELECT u.username, r.role_name 
FROM sys_user u 
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id;
```

---

## 快速修复步骤

1. 重置 admin 密码：
   ```bash
   mysql -u root -p blog < reset-admin-password.sql
   ```

2. 登录：
   - 用户名：`admin`
   - 密码：`123456`

3. 如果还是登录失败，检查：
   - 后端服务是否启动
   - 滑块验证码是否已关闭
   - 用户状态是否正常
