# 访问统计功能指南

## 完成内容

### 1. 中国地图引入
- 下载了中国地图 GeoJSON 数据
- 文件位置：`blog-admin/src/assets/map/china.json`
- 在 LogStatistics.vue 中引入并注册

### 2. 定时任务清理日志
- 在 TaskQuartz 中添加了 `cleanOperateLog()` 和 `cleanUserLog()` 方法
- 添加了 SQL：`clean-log-job.sql`
- 默认每月1号凌晨3点清理30天前的日志

### 3. 统计页面
- 在 Dashboard 中添加了 Tab 切换
- 「概览」- 原有的数据卡片和图表
- 「访问统计」- 新增的统计面板

---

## 使用步骤

### 1. 执行 SQL 脚本

```bash
# 添加定时任务
mysql -u root -p blog < clean-log-job.sql

# 重构日志表（如果还没执行）
mysql -u root -p blog < operate-log-refactor.sql
```

### 2. 重启后端服务

```bash
cd C:\Users\Chen\Desktop\shiyi-blog\blog
mvn clean compile
mvn spring-boot:run -pl mojian-server
```

### 3. 访问统计页面

1. 登录后台管理
2. 点击左侧菜单「首页」或「Dashboard」
3. 点击顶部 Tab「访问统计」

---

## 功能说明

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

### 定时任务

**清理操作日志：**
- Cron：`0 0 3 1 * ?`（每月1号凌晨3点）
- 调用方法：`task.cleanOperateLog`
- 清理规则：删除30天前的日志

**清理用户日志：**
- Cron：`0 30 3 1 * ?`（每月1号凌晨3点30分）
- 调用方法：`task.cleanUserLog`
- 清理规则：删除30天前的用户日志

---

## 后台立即清理

在后台管理 → 系统工具 → 定时任务 中：

1. 找到「清理操作日志」或「清理用户日志」任务
2. 点击「操作」→「立即执行」

---

## 相关文件

| 文件 | 说明 |
|------|------|
| `blog-admin/src/assets/map/china.json` | 中国地图 GeoJSON |
| `blog-admin/src/views/dashboard/index.vue` | Dashboard（添加Tab） |
| `blog-admin/src/views/dashboard/components/LogStatistics.vue` | 统计面板 |
| `blog/mojian-quartz/.../TaskQuartz.java` | 定时任务（清理方法） |
| `clean-log-job.sql` | 定时任务数据 |
| `operate-log-refactor.sql` | 日志表重构 |
| `LOG-REFACTOR-GUIDE.md` | 日志重构指南 |
