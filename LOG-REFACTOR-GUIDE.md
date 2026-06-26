# 操作日志系统重构指南

## 完成内容

### 1. 数据库重构

**执行 SQL：**
```bash
mysql -u root -p blog < operate-log-refactor.sql
```

**新增字段：**
- `type` - 日志类型（admin/user）
- `module` - 模块名称
- `request_method` - 请求方式
- `response_code` - 响应状态码
- `error_msg` - 错误信息
- `user_agent` - User-Agent
- `device_type` - 设备类型（PC/Mobile/Tablet）
- `os` - 操作系统
- `browser` - 浏览器类型
- `ip_source` - IP来源

### 2. 后端代码

**新建文件：**
- `UserLogger.java` - 用户端日志注解
- `UserLoggerAspect.java` - 用户端日志切面
- `SysLogStatisticsController.java` - 统计接口

**修改文件：**
- `SysOperateLog.java` - 实体类（新增字段）
- `SysOperateLogService.java` - 服务接口（新增统计方法）
- `SysOperateLogServiceImpl.java` - 服务实现（新增统计实现）
- `SysOperateLogController.java` - 控制器（新增统计接口）
- `OperationLoggerAspect.java` - 管理端切面（适配新字段）

**加注解的 Controller：**
- `UserController` - 个人中心（修改资料、删除评论）
- `CommentController` - 评论（添加评论）

### 3. 前端代码

**新建文件：**
- `blog-admin/src/views/system/log/user-log/index.vue` - 用户日志页面
- `blog-admin/src/views/dashboard/components/LogStatistics.vue` - 统计面板

### 4. 菜单配置

**用户日志菜单：**
- 在日志管理子级，和操作日志平级
- 权限：`log:userLog:list`、`log:userLog:delete`、`log:userLog:clean`

---

## 使用说明

### 1. 执行数据库脚本

```bash
mysql -u root -p blog < operate-log-refactor.sql
```

### 2. 重启后端服务

```bash
cd C:\Users\Chen\Desktop\shiyi-blog\blog
mvn clean compile
mvn spring-boot:run -pl mojian-server
```

### 3. 后台配置菜单

**用户日志菜单：**
- 路径：`/system/log/user-log/index`
- 父菜单：日志管理（ID=32）
- 权限标识：留空（按钮权限单独配置）

**统计面板菜单（可选）：**
- 路径：`/dashboard`
- 组件：`dashboard/index`
- 在统计面板中引入 `LogStatistics` 组件

---

## 功能说明

### 用户日志页面

**功能：**
- 列表展示所有用户操作日志
- 按用户名、操作名称、模块、请求方式筛选
- 按时间范围筛选
- 查看详细信息（展开行）
- 删除/批量删除
- 清空日志

**字段说明：**
| 字段 | 说明 |
|------|------|
| 用户名 | 操作用户 |
| 操作名称 | 注解中配置的操作名称 |
| 模块 | 注解中配置的模块名称 |
| 请求接口 | API 路径 |
| 请求方式 | GET/POST/PUT/DELETE |
| IP | 用户IP地址 |
| IP来源 | 省份/城市 |
| 设备 | PC/Mobile/Tablet |
| 浏览器 | Chrome/Firefox/Safari 等 |
| 系统 | Windows/macOS/Android 等 |
| 状态码 | 200/500 等 |
| 耗时 | 请求处理时间（毫秒） |

### 统计面板

**统计卡片：**
- 今日访问量
- 总访问量
- 今日用户数
- 总用户数

**图表：**
- 访问趋势（折线图，支持7天/30天切换）
- 设备分布（饼图）
- 浏览器分布（饼图）
- 操作系统分布（饼图）
- 地区分布（中国地图热力图）

---

## 给其他 Controller 加注解

在需要记录日志的方法上添加 `@UserLogger` 注解：

```java
import com.mojian.annotation.UserLogger;

@PostMapping("/add")
@UserLogger(value = "添加评论", module = "评论")
public Result<Void> add(@RequestBody SysComment sysComment) {
    // 业务逻辑
}
```

**常用模块名称：**
- 个人中心
- 评论
- 留言
- 友链
- 文章
- 通知

---

## 定时清理日志

**方式1：使用系统自带的定时任务功能**

在后台管理 → 系统工具 → 定时任务 中添加：
- 任务名称：清理用户日志
- 调用方式：Bean
- 调用目标：sysOperateLogService.cleanOldLogs
- Cron表达式：`0 0 3 1 * ?`（每月1号凌晨3点）

**方式2：手动清理**

```sql
-- 清理30天前的日志
DELETE FROM sys_operate_log WHERE create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 清理所有用户日志
DELETE FROM sys_operate_log WHERE type = 'user';

-- 清空所有日志
TRUNCATE TABLE sys_operate_log;
```

---

## 相关文件

| 文件 | 说明 |
|------|------|
| `operate-log-refactor.sql` | 数据库脚本 |
| `LOG-REFACTOR-GUIDE.md` | 本文档 |
| `UserLogger.java` | 用户端日志注解 |
| `UserLoggerAspect.java` | 用户端日志切面 |
| `SysLogStatisticsController.java` | 统计接口 |
| `user-log/index.vue` | 用户日志页面 |
| `LogStatistics.vue` | 统计面板组件 |
