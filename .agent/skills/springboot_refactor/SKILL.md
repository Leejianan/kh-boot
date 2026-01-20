---
name: SpringBoot重构指南
description: 标准化的 Spring Boot 应用重构工作流，确保代码一致性和整洁度。
---

# Spring Boot 重构指南

当用户要求“重构”或“清理” Spring Boot 模块时，请使用此 Skill。

## 重构检查清单

### 1. 实体 (Entity) & Lombok
- [ ] 确保所有实体使用 Lombok 的 `@Data` 或 `@Getter/@Setter` 以减少样板代码。
- [ ] 如果实体共享 `createTime`、`updateTime` 等字段，应继承 `BaseEntity`（或同等父类）。
- [ ] 正确使用 Mybatis-Plus/JPA 的 `@TableName`, `@TableId`, `@TableField` 注解。

### 2. 控制层 (Controller)
- [ ] 使用 `@RestController` 和 `@RequestMapping`。
- [ ] **返回类型**: 所有接口 **必须** 返回统一的响应包装类，例如 `Result<T>` 或 `AjaxResult`。
  - ❌ `public List<User> list()`
  - ✅ `public Result<List<User>> list()`
- [ ] **依赖注入**: 尽可能在字段上使用构造器注入 (`@RequiredArgsConstructor`) 代替 `@Autowired`。

### 3. 服务层 (Service)
- [ ] 推荐使用 接口 + 实现类 的模式，但对于简单逻辑不做强制要求。
- [ ] 事务管理: 在写入操作上添加 `@Transactional(rollbackFor = Exception.class)`。

### 4. 日志与格式化
- [ ] 使用 `@Slf4j` 进行日志记录。
- [ ] 删除所有 `System.out.println`。
- [ ] 根据 Google Java Style 或项目的 `.editorconfig` 格式化代码。

### 5. 导入 (Imports)
- [ ] **避免全限定名**: 在代码体中 **不要** 使用全限定类名（例如 `com.example.MyClass`）。除非发生类名冲突，否则必须添加 `import` 语句并使用简单类名。

## 示例指令
"请使用 **Spring Boot 重构指南** skill 重构 `OrderController`。"
