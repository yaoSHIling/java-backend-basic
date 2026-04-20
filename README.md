# java-backend-basic

Java 后端基础框架学习项目

## 技术栈

- **Spring Boot 3.2.4**
- **Spring Data JPA** — ORM 持久层
- **MySQL** — 关系型数据库
- **Lombok** — 简化代码
- **Validation** — 参数校验
- **Java 17**

## 项目结构

```
src/main/java/com/example/basic/
├── Application.java          # 启动类
├── controller/               # 控制层
│   └── UserController.java
├── service/                  # 服务层（接口 + 实现）
│   ├── UserService.java
│   └── impl/UserServiceImpl.java
├── dao/                      # 数据访问层（JPA Repository）
│   └── UserDao.java
└── model/                    # 实体类 + 通用响应
    ├── User.java
    └── ApiResponse.java
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 5.7+

### 2. 创建数据库

```sql
CREATE DATABASE java_backend_basic DEFAULT CHARACTER SET utf8mb4;
```

### 3. 配置数据库密码

修改 `src/main/resources/application.yml` 中的密码，或设置环境变量：

```bash
export DB_PASSWORD=你的密码
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

服务启动后访问：`http://localhost:8080`

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/list` | 获取用户列表 |
| GET | `/api/user/{id}` | 获取单个用户 |
| POST | `/api/user` | 添加用户 |
| PUT | `/api/user/{id}` | 更新用户 |
| DELETE | `/api/user/{id}` | 删除用户 |
| GET | `/api/hello` | Hello World 测试 |
