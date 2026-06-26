# 博客系统待办事项

---

## 📋 待定功能

### 1. 微信公众号登录功能

**状态**：待定  
**优先级**：P1  
**预计时间**：1-2天

#### 需求描述

实现微信公众号验证码登录功能，用户通过关注公众号发送验证码完成登录。

#### 技术方案

**使用个人订阅号**（免费，无需企业资质）

**登录流程：**
```
1. 用户点击「微信登录」
2. 显示公众号二维码 + 验证码（如 DL1234）
3. 用户关注公众号
4. 自动回复：请发送验证码 DL1234
5. 用户发送：DL1234
6. 系统验证成功，自动登录
```

#### 配置信息

编辑 `blog/mojian-server/src/main/resources/application-dev.yml`：

```yaml
wechat:
  app-id: 你的订阅号AppID
  secret: 你的订阅号AppSecret
  token: 你设置的Token（如 myblog2025）
  aesKey: 消息加解密密钥
```

#### 实施步骤

- [ ] 1. 注册个人订阅号
  - 访问 https://mp.weixin.qq.com/
  - 选择「订阅号」注册
  - 选择「个人」主体

- [ ] 2. 获取配置信息
  - 登录公众平台
  - 进入「开发」→「基本配置」
  - 记录 AppID 和 AppSecret

- [ ] 3. 配置服务器
  - 进入「开发」→「基本配置」→「服务器配置」
  - URL：`http://你的域名/wechat`
  - Token：`myblog2025`
  - EncodingAESKey：随机生成

- [ ] 4. 设置关注回复
  - 进入「自动回复」→「被关注回复」
  - 添加回复内容（引导用户登录）

- [ ] 5. 修改配置文件
  - 编辑 `application-dev.yml`
  - 填入 AppID、AppSecret、Token、AESKey

- [ ] 6. 重启后端服务
  ```bash
  cd C:\Users\Chen\Desktop\shiyi-blog\blog
  mvn spring-boot:run -pl mojian-server
  ```

- [ ] 7. 测试登录功能
  - 点击微信登录
  - 扫码关注公众号
  - 发送验证码
  - 验证登录成功

#### 参考资料

- 微信公众平台：https://mp.weixin.qq.com/
- 微信公众号开发文档：https://developers.weixin.qq.com/doc/

#### 备注

- 个人订阅号完全够用，无需服务号
- 不需要网页授权、模板消息等高级功能
- 消息会折叠在「订阅号消息」中

---

## 📝 已完成功能

### 2025-06-25

- [x] 本地文件存储配置（路径：D:\Chen\Pictures\dev_temp）
- [x] Sa-Token 拦截器配置（排除本地文件路径）
- [x] Bangumi 追番功能（支持多镜像站）
- [x] 公告组件优化（Apple Liquid Glass 风格）
- [x] 追番页面标题边距优化

---

## 🎯 未来计划

### 优先级 P0

- [ ] 文章卡片设计优化
- [ ] 搜索功能增强（Ctrl+K）
- [ ] 暗色模式优化
- [ ] 404 页面升级

### 优先级 P1

- [ ] 文章详情页优化
- [ ] 性能优化
- [ ] SEO 优化
- [ ] 阅读体验优化

### 优先级 P2

- [ ] 性能监控页面
- [ ] PWA 支持
- [ ] 评论表情包

---

**最后更新**：2025-06-25
