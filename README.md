# 🔥 Java 后端快速开发脚手架

> 面向 **OA / CRM / 后台管理 / 接口服务** 的企业级后端脚手架。
> 基于 Spring Boot 3 + MyBatis-Plus + JWT + Redis，开箱即用。

[![Java 21](https://img.shields.io/badge/Java-21-green.svg)](https://adoptium.net/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)

---

## 📌 项目定位

企业级后端快速开发脚手架，让新项目从零到上线**缩短至数小时**。

- ✅ 开箱即用 · 代码即文档 · 常见功能内置
- ✅ **Coze AI 工作流集成** · **多渠道通知** · **Excel 导入导出**

---

## 🛠️ 技术栈

| 分类 | 技术 | 说明 |
|------|------|------|
| 基础框架 | Spring Boot 3.2.4 + JDK 21 | 最新 LTS |
| ORM | MyBatis-Plus 3.5.6 | 增删改查不用手写 SQL |
| 数据库 | MySQL 8.0 | 主数据存储 |
| 缓存 | Redis 7 | Token / 缓存 / 防重复提交 |
| 认证 | JWT (jjwt 0.12.5) | 无状态接口认证 |
| 文档 | SpringDoc OpenAPI 3 | 自动生成 Swagger 文档 |
| 工具库 | Hutool 5.8 + OkHttp 4.12 | 字符串/日期/加密/HTTP/AI集成 |
| JSON | FastJSON2 2.0 | 高性能 JSON 序列化 |

---

## 🚀 快速开始

```bash
# Docker Compose 一键启动
docker-compose -f docker/docker-compose.yml up -d
# 访问：http://localhost:8080/api/swagger-ui.html

# 本地开发
# 1. MySQL 执行 sql/init.sql
# 2. 配置环境变量（见下面常用配置）
# 3. mvn spring-boot:run
```

---

## 📁 项目结构

```
src/main/java/com/example/basic/
├── util/                         # 工具类：StrUtil/DateUtil/SecureUtil/CollUtil/VerifyCode/Retrying
├── annotation/                   # @Login / @NoRepeatSubmit / @LogOperation
├── filter/                       # XssFilter / JwtAuthFilter
├── modules/
│   ├── auth/                    # 登录 / 注册
│   ├── user/                   # 用户 CRUD + 分页
│   ├── file/                    # 文件上传 / 下载
│   ├── dict/                    # 数据字典
│   ├── config/                 # 系统参数配置
│   ├── log/                    # 操作日志
│   ├── coze/                   # Coze AI 工作流（对话/触发/轮询）
│   ├── notification/            # 多渠道通知（钉钉/Server酱/飞书/邮件）
│   └── excel/                  # Excel 导入导出
```

---

## 🔑 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |

---

## 🧩 特色模块

### Coze AI 工作流（重点）

```bash
# application.yml 配置
coze:
  enabled: true
  base-url: https://api.coze.cn
  api-token: your-personal-access-token
  workflow-id: your-workflow-id
```

```java
// 调用示例
@Resource private CozeService cozeService;

// 1. 快捷对话
String reply = cozeService.chat("帮我写一段 Python");

// 2. 触发工作流（异步）
CozeWorkflowResponse trigger = cozeService.triggerWorkflow(request);
String workflowRunId = trigger.getWorkflowRunId();

// 3. 轮询结果
CozeWorkflowResponse result = cozeService.pollWorkflowResult(workflowRunId);
if (result.isCompleted()) {
    String output = result.getOutputText(); // 工作流输出
}
```

### 多渠道通知

```java
// 一键告警
notificationService.sendAlert("系统告警", "CPU 使用率超过 90%！");

// 快捷发送
notificationService.send("dingtalk", "标题", "内容");
```

### Excel 导入导出

```java
// 导出
excelService.export(userList, response, "用户列表", "用户");

// 导入
List<User> imported = excelService.importExcel(User.class, file, 0);
```

### 重试工具

```java
// 指数退避重试
String result = RetryingUtil.withExponentialBackoff(
    () -> cozeService.triggerWorkflow(request),
    5,       // 最多5次
    1000     // 基础间隔1秒，自动翻倍
);
```

---

## ⚙️ 常用环境变量

```bash
# 数据库
DB_HOST=localhost     DB_PORT=3306    DB_PASSWORD=xxx

# Redis
REDIS_HOST=localhost  REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret    JWT_EXPIRATION=604800000

# Coze
COZE_ENABLED=true
COZE_API_TOKEN=your-token
COZE_WORKFLOW_ID=your-workflow-id

# 通知
DINGTALK_ENABLED=true
DINGTALK_WEBHOOK_URL=https://oapi.dingtalk.com/robot/send?access_token=xxx
SERVERCHAN_ENABLED=true
SERVERCHAN_SENDKEY=your-sendkey
```

---

## 📖 文档地址

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API JSON**: http://localhost:8080/api/v3/api-docs
- **Druid监控**: http://localhost:8080/api/druid/ （admin / admin123）

---

## 📄 License

MIT © yaoSHIling
