# 任务：将后端项目升级至 Spring Boot 3

该任务旨在将现有的 Spring Boot 2.7.18 项目升级到 Spring Boot 3.x，利用 JDK 17 的特性，并解决潜在的安全漏洞。

## 升级步骤

- [ ] **1. 基础配置更新** <!-- id: 0 -->
    - 更新 `pom.xml` 中的 `parent` 版本至 3.x。
    - 检查并更新 Java 版本至 17（已满足）。
- [ ] **2. 依赖项兼容性调整** <!-- id: 1 -->
    - 升级 MyBatis-Plus 至 3.5.3.x+。
    - 升级 SpringDoc (Swagger) 至 v2。
    - 处理 Activiti 与 Spring Boot 3 的兼容性。
- [ ] **3. 命名空间大迁移 (javax -> jakarta)** <!-- id: 2 -->
    - 全局替换 `import javax.servlet.*` 为 `import jakarta.servlet.*`。
    - 处理 `javax.persistence` 等其他 EE 相关包。
- [ ] **4. Spring Security 6 配置适配** <!-- id: 3 -->
    - 重写 Security 配置类，使用 Lambda 风格的 DSL。
    - 调整授权与认证拦截逻辑。
- [ ] **5. 代码修复与测试** <!-- id: 4 -->
    - 解决由于 API 变更导致的编译错误。
    - 启动项目并进行接口联机测试。
