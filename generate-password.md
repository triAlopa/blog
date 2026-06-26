# 生成 BCrypt 密码

## 方法1：使用在线工具
访问：https://bcrypt-generator.com/
输入密码，点击 Generate

## 方法2：使用 Java 代码
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "你的密码";  // 修改这里
        String encodedPassword = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密后: " + encodedPassword);
    }
}
```

## 常用密码的 BCrypt 值

| 原始密码 | BCrypt 加密值 |
|---------|---------------|
| 123456 | `$2a$10$GSHv.XwqBkizplz5j2pcmu73IRY2rgtxCYQwAvSXMvu9SryzydLpe` |
| admin123 | `$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfRGBB6Qsyyp4I1xBMDZi` |
| password | `$2a$10$GSHv.XwqBkizplz5j2pcmu73IRY2rgtxCYQwAvSXMvu9SryzydLpe` |

## 使用方法

1. 选择一个密码（比如 123456）
2. 复制对应的 BCrypt 值
3. 在 SQL 中使用：

```sql
-- 添加用户
INSERT INTO sys_user (username, password, nickname, status, create_time, update_time)
VALUES ('你的用户名', '复制的BCrypt值', '你的昵称', 1, NOW(), NOW());

-- 分配角色（2=普通用户）
INSERT INTO sys_user_role (user_id, role_id) VALUES (LAST_INSERT_ID(), 2);
```
