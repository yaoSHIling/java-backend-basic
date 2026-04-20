# 🔥 Java 后端快速开发脚手架

> 面向 **OA / CRM / 后台管理 / 接口服务** 等常见系统的 Java 企业级后端脚手架。
> 基于 Spring Boot 3 + MyBatis-Plus + JWT + Redis，开箱即用，代码即文档。

[![Java 21](https://img.shields.io/badge/Java-21-green.svg)](https://adoptium.net/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 项目定位

本项目是一套**企业级后端开发脚手架**，目标：让新项目从零到上线**缩短至数小时**。

- ✅ **开箱即用**：配置好数据库连接即可运行
- ✅ **代码即文档**：Swagger UI 完整覆盖所有接口
- ✅ **常见功能内置**：用户管理、登录认证、文件上传、数据字典、系统配置、操作日志
- ✅ **工程规范**：分层结构、事务管理、全局异常、参数校验、统一响应
- ✅ **生产可用**：Druid 连接池监控、线程池、异步任务、逻辑删除

---

## 🛠️ 技术栈

| 分类 | 技术 | 说明 |
|------|------|------|
| 基础框架 | Spring Boot 3.2.4 + JDK 21 | 最新 LTS |
| ORM | MyBatis-Plus 3.5.6 | 增删改查不用手写 SQL |
| 数据库 | MySQL 8.0 | 主数据存储 |
| 缓存 | Redis 7 | Token 存储 / 缓存 / 防重复提交 |
| 认证 | JWT (jjwt 0.12.5) | 无状态接口认证 |
| 文档 | SpringDoc OpenAPI 3 | 自动生成 Swagger 文档 |
| 连接池 | Druid 1.2.21 | 数据库连接池 + SQL 监控 |
| 工具库 | Hutool 5.8 | 字符串/日期/加密/HTTP 工具集 |
| JSON | FastJSON2 2.0 | 高性能 JSON 序列化 |

---

## 📁 项目结构

```
src/main/java/com/example/basic/
│
├── Application.java                        # 启动入口
│
├── common/                                 # ============ 公共基础设施 ============
│   ├── result/
│   │   ├── Result.java                     # 统一响应封装（泛型）
│   │   └── ResultCode.java                  # 响应码枚举（HTTP语义 + 业务码）
│   ├── exception/
│   │   └── GlobalExceptionHandler.java      # 全局异常处理器
│   └── config/
│       ├── WebMvcConfig.java               # 静态资源 / 路径映射
│       ├── CorsConfig.java                  # 跨域配置
│       ├── SwaggerConfig.java               # Swagger 3 文档
│       ├── JacksonConfig.java               # JSON 日期处理
│       ├── MybatisPlusConfig.java           # 分页插件 + 自动填充
│       └── ThreadPoolConfig.java            # 线程池
│
├── model/
│   ├── entity/BaseEntity.java               # 基类（id/创建时间/更新时间/逻辑删除）
│   └── query/PageParams.java                # 分页参数
│
├── util/                                   # ============ 工具类 ============
│   ├── JwtUtil.java                         # JWT 生成/解析/验证
│   ├── AESUtil.java                         # AES 对称加密
│   ├── SnowflakeIdUtil.java                 # 雪花算法分布式ID
│   ├── IpUtil.java                          # IP地址获取（支持代理）
│   └── XssUtil.java                         # XSS HTML转义
│
├── annotation/                              # ============ 注解 ============
│   ├── Login.java                           # @Login 需登录
│   ├── NoRepeatSubmit.java                  # 防重复提交（Redis）
│   ├── LogOperation.java                    # 操作日志自动记录
│   └── Permission.java                      # 权限校验
│
├── filter/                                  # ============ 过滤器 ============
│   ├── XssFilter.java                       # XSS脚本注入过滤
│   ├── XssRequestWrapper.java               # 参数自动转义包装器
│   └── JwtAuthFilter.java                   # JWT Token验证
│
├── aspect/                                  # ============ AOP ============
│   ├── LogOperationAspect.java              # 操作日志自动记录
│   └── NoRepeatSubmitAspect.java            # Redis防重复提交
│
└── modules/                                 # ============ 业务模块 ============
    ├── auth/         # 认证：登录/注册/当前用户
    ├── user/         # 用户管理：CRUD/分页/状态
    ├── file/         # 文件上传：上传/下载/删除
    ├── dict/         # 数据字典：下拉选项/运行时枚举
    ├── config/       # 系统配置：键值对/功能开关
    └── log/          # 操作日志：查询/统计
```

---

## 🚀 快速开始

### Docker Compose 一键启动（推荐）

```bash
git clone https://github.com/yaoSHIling/java-backend-basic.git
cd java-backend-basic
docker-compose -f docker/docker-compose.yml up -d

# 访问
open http://localhost:8080/api/swagger-ui.html
```

### 本地开发

```bash
# 1. 环境：JDK 21+ · Maven 3.9+ · MySQL 5.7+ · Redis 3+

# 2. MySQL 执行初始化脚本
mysql -u root -p < sql/init.sql

# 3. 配置环境变量
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=root
export DB_PASSWORD=your_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=your-production-secret

# 4. 启动
mvn spring-boot:run
```

---

## 🔑 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 测试用户 | test | 123456 |

---

## 📋 核心接口

| 模块 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 认证 | `/api/auth/login` | POST | 登录 |
| 认证 | `/api/auth/register` | POST | 注册 |
| 认证 | `/api/auth/me` | GET | 当前用户 |
| 用户 | `/api/user/page` | GET | 分页查询 |
| 用户 | `/api/user/{id}` | GET/POST/PUT/DELETE | CRUD |
| 文件 | `/api/file/upload` | POST | 上传文件 |
| 字典 | `/api/dict/options?types=gender,user_status` | GET | 下拉选项 |
| 配置 | `/api/config/all` | GET | 所有配置 |
| 日志 | `/api/log/page` | GET | 日志查询 |

---

## 📖 文档地址

启动后访问：

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API JSON**: http://localhost:8080/api/v3/api-docs
- **Druid监控**: http://localhost:8080/api/druid/ （admin / admin123）
- **健康检查**: http://localhost:8080/api/actuator/health

---

## ⚙️ 常用环境变量

```bash
DB_HOST=localhost          # 数据库地址
DB_PORT=3306               # 数据库端口
DB_NAME=java_backend_basic # 数据库名
DB_USER=root               # 数据库用户名
DB_PASSWORD=xxx             # 数据库密码

REDIS_HOST=localhost        # Redis地址
REDIS_PORT=6379            # Redis端口

JWT_SECRET=your-production-random-secret-key  # JWT密钥（生产必改！）
JWT_EXPIRATION=604800000    # Token有效期（毫秒，默认7天）

FILE_UPLOAD_DIR=./uploads   # 文件上传目录
SERVER_PORT=8080           # 服务端口
```

---

## 🔧 新增业务模块示例

以"订单模块"为例：

```bash
# 1. 创建目录
mkdir -p src/main/java/com/example/basic/modules/order/{controller,service,dao,impl,entity}

# 2. 编写实体（继承 BaseEntity，自动获得 id/createTime/updateTime/deleted）
# src/main/java/com/example/basic/modules/order/entity/Order.java

# 3. 编写 Mapper（继承 BaseMapper，无需手写 CRUD SQL）
# src/main/java/com/example/basic/modules/order/dao/OrderDao.java

# 4. 编写 Service
# src/main/java/com/example/basic/modules/order/service/OrderService.java

# 5. 编写 Controller（加 @Tag/@Operation 注解即自动生成 Swagger 文档）
# src/main/java/com/example/basic/modules/order/controller/OrderController.java

# 6. 重启服务，接口自动出现在 Swagger UI 中
```

---

## 📄 License

MIT © yaoSHIling
