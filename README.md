# KH-Boot 企业级快速开发脚手架 (Starter 版)

`KH-Boot` 是一款基于 Spring Boot 3.x 构建的高性能、工业级权限管理系统 (RBAC) 依赖模块。它不仅提供了完善的基础功能，更通过 **Spring Boot Starter** 模式实现了零代码侵入的集成体验，旨在为中大型项目提供稳健、专业且易于扩展的底层架构支撑。

---

## ✨ 核心特性

- **🛡️ 模块化 RBAC 系统**：基于 `KhUser`、`KhRole` 和 `KhPermission` 实现完整的权限体系。支持按钮级粒度控制，内置动态路由树生成。
- **🔢 业务流水号引擎**：高精度、可配置的业务编码生成器（如 `U-20240114-0001`）。支持按日、按月自动重置，支持 Redis 原子计数与数据库持久化双重保障。
- **🔌 零侵入 Starter 模式**：完全遵循 Spring Boot 3 自动配置规范。新项目只需引入依赖，即可全自动装配缓存、安全、持久化等所有核心组件。
- **⚙️ 智能化预扫描机制**：系统启动时自动扫描标记有 `@BusinessCode` 的实体，自动注册流水号生成规则，无需手动配置。
- **📧 邮件审计追踪**：全自动记录邮件发送日志（收件人、标题、内容、结果、失败原因），支持失败回溯与可视化查询。
- **🔑 增强型会话安全**：
  - **多模态认证**：同时支持 **JSON 账号密码登录** 与 **SMS 短信验证码登录**。
  - **安全传输**：密码支持 RSA 非对称加密传输，存储采用 BCrypt 强哈希。
  - **严格管控**：配合 **Caffeine 本地缓存** 实现严格的 Token 活跃续期与强制下线机制。
- **🖥️ 实时在线监控**：可视化管理所有当前登录用户（IP、浏览器、OS、登录时间等），支持管理员一键强踢。
- **⚡ 高性能支撑**：
  - **雪花主键**：全系统使用分布式的雪花 ID 体系。
  - **MapStruct 映射**：高性能、编译期生成的 Bean 转换逻辑。
  - **Knife4j 文档**：集成了增强型 Swagger UI，支持离线文档与接口调试。

---

## 🛠 技术栈

| 核心框架 | 版本 | 描述 |
| :--- | :--- | :--- |
| **Java** | 17 | 现代 LTS 运行环境 |
| **Spring Boot** | 3.2.0 | 主流微服务底座 |
| **MyBatis Plus** | 3.5.5 | 极致效率的 ORM 增强 |
| **Caffeine** | 2.x | 工业级本地高性能缓存 |
| **Knife4j** | 4.4.0 | 优雅的 OpenAPI 3 交互文档 |
| **Redis** | 7.x | (可选) 支持分布式流水号生成 |

---

## 📁 项目目录结构

```text
com.kh.boot
├── annotation        # 自定义注解（如 @BusinessCode 业务编码标记）
├── cache             # 缓存层（AuthCache 接口及其实现）
├── config            # 自动配置入口（KhBootAutoConfiguration）
├── controller        # RESTful API 层（所有接口均采用 Kh 前缀体系）
├── converter         # MapStruct 对象转换
├── dto               # Kh 品牌化 DTO（支持 Swagger 描述）
├── entity            # Kh 品牌化 Entity（映射 kh_ 前缀数据库表）
├── mapper            # 数据访问层
├── runner            # 启动任务（实体预扫描、规则预热）
├── service           # 核心业务逻辑实现
├── util              # 架构级工具类（SerialNumberGenerator, SecurityUtils）
└── KhBootConfig      # 模块化配置入口（无 main 方法，专为库设计）
```

---

## 🚀 快速集成

### 1. 数据库初始化
1. 创建数据库并执行 [init.sql](file:///Users/harlan/dev/Idea/kh-boot/src/main/resources/sql/init.sql)。
2. 数据库表均已升级为 `kh_` 前缀，确保护航品牌化架构。

### 2. 引入依赖
将 `kh-boot` 安装至本地/私服 Maven 仓库：
```xml
<dependency>
    <groupId>com.kh</groupId>
    <artifactId>kh-boot</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 3. 配置自动激活
`kh-boot` 采用 Spring Boot 3 的 `AutoConfiguration.imports` 机制。您只需在业务项目中引入依赖，系统将自动扫描 `com.kh.boot` 包下的所有 Bean。

---

## 📖 核心 API 指南

### 🔐 认证与安全

| 接口描述 | 方法 | 路径 | 参数 |
| :--- | :--- | :--- | :--- |
| **账号登录** | POST | `/admin/auth/login` | JSON: `{"username": "...", "password": "..."}` |
| **发送验证码** | POST | `/admin/auth/sms/code` | Param: `phone` |
| **短信登录** | POST | `/admin/auth/login/sms` | Param: `phone`, `code` |
| **邮件记录** | GET | `/email/record/list` | Param: `page`, `size` |

> **SMS 集成说明**：
> 系统内置了 `MockSmsService`，会在控制台打印生成的验证码（方便测试）。
> 生产环境请实现 `SmsService` 接口并标注 `@Primary` 或 `@Service`，Sping 会自动覆盖默认的 Mock 实现。

### 🔢 业务流水号
在实体类字段上标注 `@BusinessCode`，系统将在执行 `EntityUtils.initInsert()` 时自动填装：
```java
@BusinessCode(prefix = "U", dateFormat = "yyyyMMdd", width = 4)
private String userCode; // 自动生成如: U-20240114-0001
```

> [!IMPORTANT]
> **扫描范围限制**：为了保证性能，系统仅会扫描 `kh.boot.serial-number.scan-packages` 配置路径下的类。如果你的实体类不在默认包（`**.entity`）下，请务必更新该配置。

### ⚙️ 关键配置

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `kh.security.rsa.public-key` | (内置默认) | **前端加密公钥**。需提供给前端 JS 库使用。 |
| `kh.security.rsa.private-key` | (内置默认) | **后端解密私钥**。生产环境务必覆盖。 |
| `kh.boot.serial-number.scan-packages` | `**.entity` | **流水号扫描包路径**。 |
| `spring.mail.host` | `smtp.163.com` | **邮件服务器**。默认集成网易邮箱，配置 `username`/`password` 即可使用。 |
| `spring.data.redis.host` | (无) | **Redis 坐标**。配置此项后，缓存与流水号生成器将自动升级为分布式版。 |
| `kh.boot.index.enabled` | `true` | **默认首页开关**。设为 `false` 可关闭默认的 `/` 欢迎页，避免与业务项目首页冲突。 |
| `kh.security.cors.enabled` | `true` | **跨域开关**。开发环境设为 `true`，生成环境如使用 Nginx 反代可设为 `false` 关闭。 |
| `kh.security.cors.allowed-origins` | `*` | **允许跨域的源**。默认为所有，生产环境建议指定具体域名，如 `https://admin.example.com`。 |
| `spring.sql.init.mode` | `always` | **SQL 初始化开关**。设为 `never` 可关闭启动时自动执行 SQL 脚本的功能。 |

---

## 🔗 文档访问
启动引用的项目后，访问：
`http://localhost:8080/doc.html`

---

> **Built with Power by KH-Boot Team**  
> 追求极致逻辑，守护架构尊严。
