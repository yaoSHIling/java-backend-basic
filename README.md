# 🔥 Java 后端快速开发脚手架

> 面向 **OA / CRM / 后台管理 / 接口服务** 的企业级 Java 后端脚手架。
> 基于 Spring Boot 3 + MyBatis-Plus + JWT + Redis，开箱即用，代码即文档。

[![Java 21](https://img.shields.io/badge/Java-21-green.svg)](https://adoptium.net/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📌 项目定位

本项目是一套**企业级后端开发脚手架**，让新项目从零到上线**缩短至数小时**。

- ✅ 开箱即用：配置数据库连接即可运行
- ✅ 代码即文档：SpringDoc OpenAPI 完整覆盖所有接口
- ✅ 常见功能内置：用户管理 / 登录认证 / 文件上传 / 数据字典 / 操作日志
- ✅ **Coze AI 工作流集成**：AI 生成内容 + 自动发布
- ✅ **多渠道通知**：钉钉 / Server酱 / 飞书 / 企业微信 / 邮件
- ✅ **Excel 导入导出**：基于 Hutool 轻量实现
- ✅ 工程规范：分层结构 / 事务管理 / 全局异常 / 参数校验 / 统一响应

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
| 工具库 | Hutool 5.8 + OkHttp 4.12 | 字符串/日期/加密/HTTP/AI集成 |
| JSON | FastJSON2 2.0 | 高性能 JSON 序列化 |

---

## 📁 项目结构

```
src/main/java/com/example/basic/
│
├── Application.java                      # 启动入口
│
├── annotation/                           # ============ 注解 ============
│   ├── Login.java                        # 登录认证（加在 Controller 方法上）
│   ├── NoRepeatSubmit.java              # 防重复提交
│   ├── LogOperation.java                 # 操作日志记录
│   └── Permission.java                  # 权限校验
│
├── aspect/                               # ============ 切面 ============
│   ├── LogOperationAspect.java          # 操作日志切面
│   └── NoRepeatSubmitAspect.java        # 防重复提交切面
│
├── common/                              # ============ 公共基础设施 ============
│   ├── config/                          # CORS / Swagger / Jackson / MyBatis-Plus / 线程池
│   ├── constant/AuthConstant.java        # 认证常量
│   ├── exception/GlobalExceptionHandler.java  # 全局异常处理器
│   └── result/                          # 统一响应 Result / ResultCode
│
├── filter/                               # ============ 过滤器 ============
│   ├── JwtAuthFilter.java               # JWT 认证过滤器
│   └── XssFilter.java                   # XSS 防护过滤器
│
├── model/                               # ============ 基础模型 ============
│   ├── entity/BaseEntity.java          # 基类（id / created / updated / deleted）
│   └── query/PageParams.java           # 分页参数
│
├── util/                                # ============ 工具类 ============
│   ├── StrUtil.java                     # 字符串：空判断/截取/格式化/脱敏/URL编码
│   ├── DateUtil.java                    # 日期：格式/解析/计算/计时器
│   ├── SecureUtil.java                  # 安全：MD5/SHA256/AES/Base64/UUID
│   ├── CollUtil.java                    # 集合：交集/差集/分组/去重
│   ├── VerifyCodeUtil.java             # 验证码：图形/算术/短信
│   └── RetryingUtil.java              # 重试：固定间隔/指数退避/熔断器
│
└── modules/                             # ============ 业务模块 ============
    ├── auth/         # 认证：登录/注册/Token刷新
    ├── user/         # 用户管理：CRUD/分页/状态/角色
    ├── file/         # 文件上传：上传/下载/删除/图片压缩
    ├── dict/         # 数据字典：下拉选项/类型管理
    ├── config/       # 系统配置：键值对配置管理
    ├── log/          # 操作日志：查询/统计/导出
    ├── coze/         # Coze AI 工作流：对话/触发/轮询
    ├── notification/  # 多渠道通知：钉钉/Server酱/飞书/邮件
    └── excel/        # Excel 导入导出
```

---

## 🚀 快速开始

### 方式一：Docker Compose（推荐）

```bash
git clone https://github.com/yaoSHIling/java-backend-basic.git
cd java-backend-basic
docker-compose -f docker/docker-compose.yml up -d

# 访问 Swagger 文档
open http://localhost:8080/api/swagger-ui.html
```

### 方式二：本地开发

```bash
# 1. 环境要求
#    JDK 21+ · Maven 3.9+ · MySQL 5.7+ · Redis 3+

# 2. MySQL 执行初始化脚本
mysql -u root -p < sql/init.sql

# 3. 配置环境变量
export DB_HOST=localhost
export DB_PORT=3306
export DB_PASSWORD=your_mysql_password
export REDIS_HOST=localhost
export JWT_SECRET=your-production-secret-change-this

# 4. 启动
mvn spring-boot:run

# 5. 访问
open http://localhost:8080/api/swagger-ui.html
```

---

## 🔑 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |

---

## 📖 接口文档地址

| 文档 | 地址 |
|------|------|
| **Swagger UI**（推荐） | http://localhost:8080/api/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api/v3/api-docs |
| **Druid 监控** | http://localhost:8080/api/druid/ （admin / admin123） |
| **Actuator 健康** | http://localhost:8080/api/actuator/health |

---

# 📚 开发手册（详细）

## 目录

- [1. 接口开发规范](#1-接口开发规范)
- [2. 新增业务模块](#2-新增业务模块)
- [3. 数据库操作](#3-数据库操作)
- [4. 认证与权限](#4-认证与权限)
- [5. 文件上传](#5-文件上传)
- [6. 数据字典](#6-数据字典)
- [7. Redis 缓存](#7-redis-缓存)
- [8. 操作日志](#8-操作日志)
- [9. 异常处理](#9-异常处理)
- [10. 多环境配置](#10-多环境配置)
- [11. 常用工具类](#11-常用工具类)
- [12. 场景案例：AI 写小说 → 自动发布到番茄小说](#12-场景案例ai-写小说--自动发布到番茄小说)

---

## 1. 接口开发规范

### 1.1 统一响应格式

所有接口统一返回 `Result<T>`：

```java
// 成功响应
return Result.success(data);
return Result.success("操作成功");
return Result.success(20001, "自定义成功码", data);

// 失败响应
return Result.fail(400, "参数错误");
return Result.fail(ResultCode.BAD_REQUEST);
return Result.fail(50000, "服务器内部错误");
```

响应结构：
```json
{
  "code": 20000,
  "msg": "success",
  "data": { ... }
}
```

### 1.2 编写新接口

```java
@Tag(name = "02. 用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    @Login                          // 需要登录
    public Result<PageResult<UserVO>> page(UserPageQuery query) {
        IPage<UserVO> page = userService.page(query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @Login
    @LogOperation("新增用户")        // 记录操作日志
    public Result<Long> save(@RequestBody @Valid UserSaveDTO dto) {
        Long id = userService.saveUser(dto);
        return Result.success(id);
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    @Login
    @LogOperation("修改用户")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid UserUpdateDTO dto) {
        dto.setId(id);
        userService.updateUser(dto);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @Login
    @LogOperation("删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
```

### 1.3 分页查询

```java
// Step 1: 定义查询参数（继承 PageParams）
@Data
public class UserPageQuery extends PageParams {
    private String username;
    private String mobile;
    private Integer status;
    private Long deptId;
}

// Step 2: Service 实现
public IPage<UserVO> page(UserPageQuery query) {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(query.getUsername() != null, User::getUsername, query.getUsername())
           .eq(query.getStatus() != null, User::getStatus, query.getStatus())
           .orderByDesc(User::getCreatedTime);

    IPage<User> page = userDao.selectPage(query.toPage(), wrapper);
    return page.convert(this::toVO);  // 转换为 VO
}

// Step 3: Controller
@GetMapping("/page")
public Result<PageResult<UserVO>> page(UserPageQuery query) {
    return Result.success(PageResult.of(userService.page(query)));
}
```

---

## 2. 新增业务模块

以新增"订单管理"模块为例：

### 2.1 创建目录结构

```
modules/order/
├── controller/OrderController.java
├── entity/Order.java                  # 继承 BaseEntity
├── dto/OrderSaveDTO.java
├── dto/OrderUpdateDTO.java
├── dto/OrderVO.java
├── dao/OrderDao.java                  # 继承 BaseDao
├── service/OrderService.java
└── impl/OrderServiceImpl.java
```

### 2.2 定义实体

```java
@Data
@TableName("ord_order")
public class Order extends BaseEntity {

    private String orderNo;        // 订单编号
    private Long userId;           // 用户ID
    private BigDecimal amount;     // 订单金额
    private Integer status;         // 订单状态：1=待付款 2=已付款 3=已完成 4=已取消
    private String remark;         // 备注
}
```

### 2.3 定义 Service

```java
public interface OrderService {

    IPage<OrderVO> page(OrderPageQuery query);

    Long saveOrder(OrderSaveDTO dto);

    void updateOrder(OrderUpdateDTO dto);

    void deleteOrder(Long id);

    OrderVO getById(Long id);
}
```

### 2.4 Service 实现（事务生效）

```java
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final ProductService productService;  // 依赖注入
    private final NotificationService notificationService;

    @Override
    public Long saveOrder(OrderSaveDTO dto) {
        // 1. 校验商品
        Product product = productService.getById(dto.getProductId());
        if (product == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "商品不存在");
        }

        // 2. 创建订单
        Order order = new Order();
        order.setOrderNo(SecureUtil.uuid().substring(0, 16).toUpperCase());
        order.setAmount(product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        order.setStatus(1);
        orderDao.insert(order);

        // 3. 发送通知
        notificationService.sendDefault("新订单",
                StrUtil.format("订单号：{}，金额：{}", order.getOrderNo(), order.getAmount()));

        return order.getId();
    }
}
```

### 2.5 数据库表

```sql
CREATE TABLE ord_order (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no    VARCHAR(32)  NOT NULL UNIQUE COMMENT '订单编号',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    amount      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '订单金额',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1=待付款 2=已付款 3=已完成 4=已取消',
    remark      VARCHAR(255) COMMENT '备注',
    created_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删 1=已删'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

---

## 3. 数据库操作

### 3.1 基础 CRUD（不用写 SQL）

```java
// 增
userDao.insert(user);

// 删
userDao.deleteById(id);
userDao.deleteBatchIds(List.of(1, 2, 3));

// 改
userDao.updateById(user);

// 查
userDao.selectById(id);
userDao.selectBatchIds(List.of(1, 2, 3));
userDao.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
```

### 3.2 条件查询

```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

wrapper.eq(User::getStatus, 1)                    // 等于
       .ne(User::getStatus, 0)                  // 不等于
       .like(User::getUsername, "admin")         // 模糊匹配
       .likeRight(User::getMobile, "138")        // 左like
       .in(User::getId, List.of(1, 2, 3))       // IN
       .between(User::getCreatedTime, start, end) // 区间
       .isNull(User::getRemark())                // IS NULL
       .orderByDesc(User::getCreatedTime)        // 倒序
       .orderByAsc(User::getSort)               // 升序
       .last("LIMIT 10");                        // 追加 SQL 尾

List<User> users = userDao.selectList(wrapper);
```

### 3.3 复杂查询（Mapper XML）

```xml
<select id="selectByComplexQuery" resultType="UserVO">
    SELECT u.*, d.name AS dept_name
    FROM sys_user u
    LEFT JOIN sys_dept d ON u.dept_id = d.id
    <where>
        <if test="username != null and username != ''">
            AND u.username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test="deptId != null">
            AND u.dept_id = #{deptId}
        </if>
        <if test="startTime != null">
            AND u.created_time &gt;= #{startTime}
        </if>
    </where>
    ORDER BY u.created_time DESC
</select>
```

---

## 4. 认证与权限

### 4.1 接口登录控制

```java
// 方式一：加 @Login 注解（最常用）
@Login
public Result<UserVO> getCurrentUser() { ... }

// 方式二：排除白名单（在 JwtAuthFilter 中配置）
// 路径 /api/auth/** /api/user/login 自动放行，无需 @Login
```

### 4.2 获取当前登录用户

```java
@Resource
private HttpServletRequest request;

// 方式一：从 Token 中获取（推荐）
LoginUser loginUser = JwtUtil.getLoginUser(request);
Long userId = loginUser.getUserId();
String username = loginUser.getUsername();

// 方式二：从 SecurityContext 获取（在 Filter 之后）
SecurityUser securityUser = (SecurityUser) request.getAttribute("user");
```

### 4.3 生成 Token

```java
// 登录时生成
String token = JwtUtil.generateToken(userId, username, roleIds);

// Token 有效期默认 7 天（可配置 jwt.expiration）
// 续期：用户活跃时自动续期（每次有效请求刷新 token 剩余时间）
```

### 4.4 接口防重复提交

```java
@PostMapping("/submit")
@NoRepeatSubmit(value = 5, unit = TimeUnit.SECONDS)  // 5秒内同一用户同一接口不能重复提交
@LogOperation("提交订单")
public Result<Long> submit(@RequestBody @Valid OrderSubmitDTO dto) {
    // 业务逻辑
}
```

---

## 5. 文件上传

### 5.1 上传接口

```bash
POST /api/file/upload
Content-Type: multipart/form-data

file: [选择文件]
```

### 5.2 返回格式

```json
{
  "code": 20000,
  "msg": "success",
  "data": {
    "url": "http://localhost:8080/api/uploads/2024/04/20/abc123.jpg",
    "filename": "photo.jpg",
    "size": 102400,
    "mimeType": "image/jpeg"
  }
}
```

### 5.3 文件访问

上传后直接通过 URL 访问：
```bash
GET /api/uploads/2024/04/20/abc123.jpg
```

---

## 6. 数据字典

### 6.1 使用场景

下拉选项、状态展示等场景使用数据字典，避免硬编码。

```bash
# 获取下拉选项
GET /api/dict/options?types=gender,order_status,user_status
```

```json
{
  "code": 20000,
  "msg": "success",
  "data": {
    "gender": [
      { "value": 1, "label": "男" },
      { "value": 2, "label": "女" }
    ],
    "order_status": [
      { "value": 1, "label": "待付款" },
      { "value": 2, "label": "已付款" }
    ]
  }
}
```

### 6.2 新增字典类型

```java
// 在 DictTypeEnum 中添加
GENDER("gender", "性别"),
ORDER_STATUS("order_status", "订单状态");

// 在 sys_dict 表中添加对应字典项
INSERT INTO sys_dict (type, value, label, sort) VALUES
  ('order_status', 1, '待付款', 1),
  ('order_status', 2, '已付款', 2),
  ('order_status', 3, '已完成', 3);
```

---

## 7. Redis 缓存

### 7.1 缓存数据

```java
@Resource
private RedisTemplate<String, Object> redisTemplate;

// 存
redisTemplate.opsForValue().set("user:100", user, Duration.ofHours(1));

// 取
User user = (User) redisTemplate.opsForValue().get("user:100");

// 删
redisTemplate.delete("user:100");

// 存在才设置（防缓存击穿）
redisTemplate.opsForValue().setIfAbsent("lock:order:1", "1",
    Duration.ofSeconds(30));
```

### 7.2 缓存注解

```java
// 缓存查询结果（3分钟）
@Cacheable(value = "user", key = "#id")
public User getById(Long id) { ... }

// 更新后删除缓存
@CacheEvict(value = "user", key = "#id")
public void updateUser(User user) { ... }

// 更新后清空所有 user 缓存
@CacheEvict(value = "user", allEntries = true)
public void updateUser(User user) { ... }
```

---

## 8. 操作日志

### 8.1 自动记录

```java
// 在需要记录的方法上加注解
@LogOperation("新增用户")
public Result<Long> save(@RequestBody @Valid UserSaveDTO dto) { ... }

@LogOperation("删除角色：${roleId}")  // 支持 SpEL 表达式
public Result<Void> deleteRole(@PathVariable Long roleId) { ... }
```

### 8.2 查询日志

```bash
GET /api/log/page?username=admin&startTime=2024-04-01&endTime=2024-04-20
```

---

## 9. 异常处理

### 9.1 业务异常

```java
// 抛出业务异常（自动被 GlobalExceptionHandler 捕获并返回统一格式）
throw new BizException(ResultCode.BAD_REQUEST, "用户名已存在");
throw new BizException(40001, "余额不足");
```

### 9.2 全局异常处理

| 异常类型 | HTTP 状态码 | 返回码 |
|----------|-------------|--------|
| BizException | 200（业务失败） | 自定义 |
| ValidationException | 200（参数校验失败） | 40000 |
| MethodArgumentNotValidException | 200 | 40000 |
| MybatisPlusException | 200 | 50001 |
| 其他未处理异常 | 200 | 50000 |

---

## 10. 多环境配置

### 10.1 配置文件

```
src/main/resources/
├── application.yml              # 默认配置（所有环境共享）
├── application-dev.yml          # 开发环境
├── application-test.yml         # 测试环境
└── application-prod.yml         # 生产环境
```

### 10.2 激活环境

```bash
# 方式一：命令行
java -jar app.jar --spring.profiles.active=prod

# 方式二：环境变量
export SPRING_PROFILES_ACTIVE=prod

# 方式三：IDEA 中配置
# Edit Configurations → Spring Boot → Active profiles: dev
```

### 10.3 环境变量示例

```bash
# .env 文件（不要提交到 Git）
DB_HOST=rm-xxx.mysql.rds.aliyuncs.com
DB_PASSWORD=prod_secret_xxx
REDIS_HOST=rm-xxx.redis.rds.aliyuncs.com
REDIS_PASSWORD=prod_redis_xxx
JWT_SECRET=very_long_random_secret_at_least_64_chars
SPRING_PROFILES_ACTIVE=prod
```

---

## 11. 常用工具类

### 11.1 StrUtil — 字符串

```java
// 空判断
if (StrUtil.isBlank(name)) return Result.fail("名称不能为空");

// 默认值
String title = StrUtil.ifBlank(inputTitle, "无标题");

// 格式化（占位符）
String msg = StrUtil.format("用户{}登录了系统", username);

// 脱敏
String phone = StrUtil.maskPhone("13812345678");  // 138****8878
String email = StrUtil.maskEmail("admin@gmail.com");  // a***n@gmail.com

// 截取
StrUtil.left("abcdef", 3);   // "abc"
StrUtil.right("abcdef", 3);  // "def"
```

### 11.2 DateUtil — 日期时间

```java
// 格式化
String now = DateUtil.format(new Date());  // "2024-04-20 15:30:00"

// 解析
Date date = DateUtil.parse("2024-04-20 15:30:00");

// 计算
Date tomorrow = DateUtil.addDays(new Date(), 1);
long days = DateUtil.diffDays(dateA, dateB);

// 计时器
StopWatch sw = DateUtil.startWatch();
// ... 执行代码 ...
long ms = sw.getTotalTimeMillis();  // 获取耗时
```

### 11.3 SecureUtil — 安全加密

```java
// 密码加密（存储）
String salt = SecureUtil.genSalt();                    // 生成盐
String hashed = SecureUtil.sha256Salt(password, salt); // 加密存储

// 密码验证（登录）
if (!SecureUtil.verifyPwd(inputPassword, salt, storedHash)) {
    throw new BizException(ResultCode.BAD_REQUEST, "密码错误");
}

// AES 加密（敏感数据）
String encrypted = SecureUtil.aesEnc(jsonData, secretKey);
String decrypted = SecureUtil.aesDec(encrypted, secretKey);

// UUID
String uuid = SecureUtil.uuid();  // "550e8400e29b41d4a716446655440000"
```

### 11.4 RetryingUtil — 重试

```java
// 指数退避重试（第三方 API 调用）
CozeWorkflowResponse result = RetryingUtil.withExponentialBackoff(
    () -> cozeService.triggerWorkflow(request),
    5,      // 最多重试5次
    1000    // 基础间隔1秒，自动翻倍：1s → 2s → 4s → 8s → 16s
);

// 固定间隔重试（网络不稳定时）
String content = RetryingUtil.withRetry(
    () -> httpClient.get(url),
    3,     // 最多3次
    2000   // 每次间隔2秒
);
```

---

## 12. 场景案例：AI 写小说 → 自动发布到番茄小说

> 以下展示一个**完整的业务场景**，包含从需求分析到代码实现的全流程。

### 12.1 场景说明

**目标：** 后端自动调用 Coze AI 工作流生成小说章节内容，存入草稿箱后推送到番茄小说平台。

**流程：**
```
定时任务触发（每天 09:30）
    ↓
调用 Coze 工作流生成章节（AI 写作）
    ↓
内容存库（本地草稿箱）
    ↓
推送到番茄小说（异步，后台执行）
    ↓
通知作者（钉钉/Server酱）
```

### 12.2 数据库设计

```sql
-- 小说章节草稿表
CREATE TABLE novel_chapter (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id        BIGINT      NOT NULL COMMENT '所属小说ID',
    chapter_no      INT         NOT NULL COMMENT '章节号',
    title           VARCHAR(200) NOT NULL COMMENT '章节标题',
    content         LONGTEXT    NOT NULL COMMENT '章节正文',
    word_count      INT         DEFAULT 0 COMMENT '字数',
    status          TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 1=已发布 2=发布失败',
    published_at    DATETIME    COMMENT '发布时间',
    coze_run_id     VARCHAR(64) COMMENT 'Coze工作流执行ID',
    error_msg       VARCHAR(500) COMMENT '失败原因',
    created_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说章节草稿';

-- 小说信息表
CREATE TABLE novel_info (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(200) NOT NULL COMMENT '书名',
    author          VARCHAR(100) NOT NULL COMMENT '作者',
    fanqie_book_id  VARCHAR(64) COMMENT '番茄小说book_id',
    fanqie_token    VARCHAR(500) COMMENT '番茄登录Token',
    last_chapter_no INT         DEFAULT 0 COMMENT '最后更新章节号',
    created_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说信息';
```

### 12.3 实现代码

#### Step 1：NovelChapter 实体

```java
@Data
@TableName("novel_chapter")
public class NovelChapter extends BaseEntity {

    private Long novelId;
    private Integer chapterNo;
    private String title;
    private String content;
    private Integer wordCount;
    private Integer status;      // 0=草稿 1=已发布 2=失败
    private Date publishedAt;
    private String cozeRunId;
    private String errorMsg;
}
```

#### Step 2：NovelChapterService

```java
public interface NovelChapterService {

    /**
     * AI 生成并保存章节草稿
     * @param novelId 小说ID
     * @param chapterNo 章节号
     * @return 章节ID
     */
    Long generateAndSaveDraft(Long novelId, Integer chapterNo);

    /**
     * 发布章节到番茄小说
     * @param chapterId 章节ID
     */
    void publishToFanqie(Long chapterId);

    /**
     * 批量发布草稿（后台异步）
     */
    void batchPublishPending();
}
```

#### Step 3：NovelChapterServiceImpl（核心逻辑）

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class NovelChapterServiceImpl implements NovelChapterService {

    private final NovelChapterDao chapterDao;
    private final NovelInfoDao novelDao;
    private final CozeService cozeService;
    private final NotificationService notificationService;

    private static final int CHAPTER_WORD_TARGET = 3000; // 每章目标字数

    @Override
    public Long generateAndSaveDraft(Long novelId, Integer chapterNo) {
        // 1. 查询小说信息
        NovelInfo novel = novelDao.selectById(novelId);
        if (novel == null) throw new BizException(40001, "小说不存在");

        // 2. 构造 Coze 工作流请求
        CozeRequest request = CozeRequest.builder()
                .workflowId("your-novel-workflow-id")  // Coze 工作流 ID
                .variables(Map.of(
                        "novel_title", novel.getTitle(),
                        "chapter_no", chapterNo,
                        "author", novel.getAuthor(),
                        "word_target", CHAPTER_WORD_TARGET
                ))
                .build();

        // 3. 调用 Coze AI 工作流生成内容（指数退避重试）
        CozeWorkflowResponse response;
        try {
            response = RetryingUtil.withExponentialBackoff(
                    () -> cozeService.triggerWorkflow(request),
                    3,    // 最多3次
                    2000  // 2秒基础间隔
            );
        } catch (Exception e) {
            log.error("AI 生成章节失败 | novelId={} | chapterNo={}", novelId, chapterNo, e);
            notificationService.sendAlert("AI 写小说失败",
                    StrUtil.format("小说：{} 第{}章生成失败\n原因：{}", 
                            novel.getTitle(), chapterNo, e.getMessage()));
            throw new BizException(50001, "AI 生成失败: " + e.getMessage());
        }

        // 4. 解析工作流输出
        if (!response.isCompleted()) {
            throw new BizException(50001, "工作流未完成，状态：" + response.getStatus());
        }

        String content = response.getOutputText();
        if (StrUtil.isBlank(content)) {
            throw new BizException(50001, "AI 返回内容为空");
        }

        // 5. 提取章节标题（工作流返回格式：标题|正文）
        String title;
        String body;
        if (content.contains("|")) {
            String[] parts = content.split("\|", 2);
            title = parts[0].trim();
            body = parts[1].trim();
        } else {
            title = StrUtil.format("第{}章", chapterNo);
            body = content;
        }

        // 6. 保存草稿
        NovelChapter chapter = new NovelChapter();
        chapter.setNovelId(novelId);
        chapter.setChapterNo(chapterNo);
        chapter.setTitle(title);
        chapter.setContent(body);
        chapter.setWordCount(StrUtil.trimAll(body).length());
        chapter.setStatus(0);  // 草稿
        chapter.setCozeRunId(response.getWorkflowRunId());
        chapterDao.insert(chapter);

        log.info("AI 生成章节草稿成功 | chapterId={} | chapterNo={} | 字数={}",
                chapter.getId(), chapterNo, chapter.getWordCount());

        return chapter.getId();
    }

    @Override
    public void publishToFanqie(Long chapterId) {
        NovelChapter chapter = chapterDao.selectById(chapterId);
        if (chapter == null) throw new BizException(40001, "章节不存在");
        if (chapter.getStatus() == 1) throw new BizException(40001, "章节已发布");

        NovelInfo novel = novelDao.selectById(chapter.getNovelId());
        if (novel == null) throw new BizException(40001, "小说不存在");

        try {
            // TODO: 调用番茄小说 API 发布章节
            // publishChapter(novel.getFanqieBookId(), novel.getFanqieToken(), chapter);

            // 模拟成功
            chapter.setStatus(1);
            chapter.setPublishedAt(new Date());
            chapterDao.updateById(chapter);

            // 更新小说最后章节号
            novel.setLastChapterNo(chapter.getChapterNo());
            novelDao.updateById(novel);

            log.info("章节发布成功 | chapterId={} | novel={}", chapterId, novel.getTitle());

        } catch (Exception e) {
            chapter.setStatus(2);  // 失败
            chapter.setErrorMsg(e.getMessage());
            chapterDao.updateById(chapter);

            // 发送告警通知
            notificationService.sendAlert("章节发布失败",
                    StrUtil.format("小说：{} 第{}章发布失败\n原因：{}",
                            novel.getTitle(), chapter.getChapterNo(), e.getMessage()));

            throw new BizException(50001, "发布失败: " + e.getMessage());
        }
    }

    @Override
    @Async                       // 异步执行，不阻塞主线程
    public void batchPublishPending() {
        // 查询所有待发布的草稿
        List<NovelChapter> pending = chapterDao.selectList(
                new LambdaQueryWrapper<NovelChapter>()
                        .eq(NovelChapter::getStatus, 0)
                        .orderByAsc(NovelChapter::getNovelId, NovelChapter::getChapterNo)
                        .last("LIMIT 50")  // 每次最多50章
        );

        if (pending.isEmpty()) {
            log.info("没有待发布的章节草稿");
            return;
        }

        int success = 0, failed = 0;
        for (NovelChapter chapter : pending) {
            try {
                publishToFanqie(chapter.getId());
                success++;
                Thread.sleep(1000);  // 每章间隔1秒，避免频率限制
            } catch (Exception e) {
                failed++;
                log.warn("章节发布异常 | chapterId={}", chapter.getId(), e);
            }
        }

        // 发送汇总通知
        notificationService.sendDefault("小说发布任务完成",
                StrUtil.format("成功：{} 章，失败：{} 章", success, failed));

        log.info("批量发布完成 | 成功={} | 失败={}", success, failed);
    }
}
```

#### Step 4：NovelChapterController

```java
@Tag(name = "09. 小说管理", description = "AI 写作 + 番茄发布")
@RestController
@RequestMapping("/novel/chapter")
@RequiredArgsConstructor
public class NovelChapterController {

    private final NovelChapterService chapterService;

    @Operation(summary = "生成章节草稿",
               description = "调用 Coze AI 工作流生成小说章节内容，存入草稿箱")
    @PostMapping("/generate")
    @Login
    @LogOperation("生成章节草稿")
    public Result<Long> generate(
            @Parameter(description = "小说ID") @RequestParam Long novelId,
            @Parameter(description = "章节号") @RequestParam Integer chapterNo) {
        Long chapterId = chapterService.generateAndSaveDraft(novelId, chapterNo);
        return Result.success(chapterId);
    }

    @Operation(summary = "发布章节到番茄小说",
               description = "将草稿箱中的章节推送到番茄小说平台")
    @PostMapping("/publish/{chapterId}")
    @Login
    @LogOperation("发布章节到番茄")
    public Result<Void> publish(@PathVariable Long chapterId) {
        chapterService.publishToFanqie(chapterId);
        return Result.success("发布成功");
    }

    @Operation(summary = "批量发布待审章节",
               description = "后台异步批量发布所有待发布的草稿章节")
    @PostMapping("/publish/batch")
    @Login
    @LogOperation("批量发布章节")
    public Result<Void> batchPublish() {
        // 异步执行，立即返回
        chapterService.batchPublishPending();
        return Result.success("发布任务已启动，请在通知中查看结果");
    }

    @Operation(summary = "查询章节列表")
    @GetMapping("/page")
    @Login
    public Result<PageResult<NovelChapter>> page(ChapterPageQuery query) {
        return Result.success(PageResult.of(chapterService.page(query)));
    }

    @Operation(summary = "查看章节详情")
    @GetMapping("/{id}")
    @Login
    public Result<NovelChapter> getById(@PathVariable Long id) {
        NovelChapter chapter = chapterService.getById(id);
        return Result.success(chapter);
    }
}
```

### 12.4 定时任务（每天自动执行）

```java
@Component
@RequiredArgsConstructor
public class NovelCronJob {

    private final NovelChapterService chapterService;
    private final NotificationService notificationService;

    /**
     * 每天 09:30 自动生成并发布新章节
     * 配置：@Scheduled(cron = "0 30 9 * * ?")
     */
    @Scheduled(cron = "0 30 9 * * ?")
    @Async
    public void autoGenerateAndPublish() {
        log.info("定时任务：AI 写小说开始");

        try {
            // 1. 查询需要更新的小说（假设 ID=1）
            Long novelId = 1L;
            NovelInfo novel = novelDao.selectById(novelId);
            int nextChapter = novel.getLastChapterNo() + 1;

            // 2. 生成新章节草稿
            Long chapterId = chapterService.generateAndSaveDraft(novelId, nextChapter);

            // 3. 自动发布到番茄
            chapterService.publishToFanqie(chapterId);

            // 4. 通知作者
            notificationService.sendDefault("小说更新完成",
                    StrUtil.format("《{}》第{}章已自动发布，字数约{}字",
                            novel.getTitle(), nextChapter, 3000));

        } catch (Exception e) {
            log.error("AI 写小说定时任务失败", e);
            notificationService.sendAlert("AI 写小说任务失败", e.getMessage());
        }
    }
}
```

### 12.5 配置（application.yml）

```yaml
# Coze AI
coze:
  enabled: true
  base-url: https://api.coze.cn
  api-token: ${COZE_API_TOKEN:}
  workflow-id: your-novel-workflow-id
  poll-interval: 2
  poll-timeout: 120   # AI 生成可能较慢，超时设长一点

# 通知
notification:
  dingtalk:
    enabled: true
    webhook-url: ${DINGTALK_WEBHOOK_URL:}
  serverchan:
    enabled: true
    sendkey: ${SERVERCHAN_SENDKEY:}
```

### 12.6 整体调用时序图

```
作者/定时任务
     │
     ▼
NovelChapterController.generate()
     │
     ▼
CozeService.triggerWorkflow()
     │  ┌──────────────────────────┐
     │  │  Coze AI 工作流          │
     │  │  （小说大纲 → 正文生成）  │
     │  └──────────────────────────┘
     ▼
NovelChapterDao.insert()   保存草稿到数据库
     │
     ▼
通知Service.sendDefault()   通知：草稿已生成
     │
     ▼
Controller 返回 chapterId
```

---

## ⚙️ 常用环境变量

```bash
# ========== 数据库 ==========
DB_HOST=localhost              # MySQL 主机
DB_PORT=3306                  # MySQL 端口
DB_NAME=java_backend_basic    # 数据库名
DB_USER=root                  # 用户名
DB_PASSWORD=xxx               # 密码

# ========== Redis ==========
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# ========== JWT ==========
JWT_SECRET=your-production-secret-at-least-64-chars
JWT_EXPIRATION=604800000      # Token 有效期（毫秒），默认7天

# ========== Coze AI ==========
COZE_ENABLED=true
COZE_BASE_URL=https://api.coze.cn
COZE_API_TOKEN=your-personal-access-token
COZE_WORKFLOW_ID=your-workflow-id
COZE_POLL_TIMEOUT=120

# ========== 通知 ==========
DINGTALK_ENABLED=true
DINGTALK_WEBHOOK_URL=https://oapi.dingtalk.com/robot/send?access_token=xxx
SERVERCHAN_ENABLED=true
SERVERCHAN_SENDKEY=your-sendkey
FEISHU_ENABLED=false
FEISHU_WEBHOOK_URL=https://open.feishu.cn/open-apis/bot/v2/hook/xxx

# ========== 服务 ==========
SERVER_PORT=8080
FILE_UPLOAD_DIR=./uploads
DRUID_PASSWORD=admin123
SPRING_PROFILES_ACTIVE=dev
```

---

## 📄 License

MIT © yaoSHIling
