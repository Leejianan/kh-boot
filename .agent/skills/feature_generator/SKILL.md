---
name: 功能模块生成器
description: 根据表名生成符合 SpringBoot 重构规范的后端 CRUD 代码及权限菜单 SQL。
---

# 功能模块生成器 (Feature Generator)

当用户提供一个数据库表名，并要求生成相关功能时，请严格按照以下步骤执行。

## 1. 准备阶段
1.  **分析表结构**: 使用工具查看目标表的结构（Columns, Types, Comments）。
2.  **确定模块信息**:
    *   **实体名**: 大驼峰命名 (e.g., `sys_log` -> `SysLog`)。
    *   **包路径**: 确认项目的基础包路径 (e.g., `com.kh.boot`)。
    *   **权限前缀**: 确定权限标识符前缀 (e.g., `system:log`).

## 2. 生成后端代码
> **重要**: 生成的代码必须严格遵守 [SpringBoot 重构指南] Skill 的规范。

### A. Entity (`entity` 包)
*   使用 Lombok `@Data`。
*   使用 MyBatis-Plus `@TableName`。
*   如果表包含 `create_time`, `update_time`, `create_by`, `update_by`，**必须**继承 `BaseEntity`。
*   字段需添加 Swagger/JavaDoc 注释 (来源于表注释)。

### B. Mapper (`mapper` 包)
*   继承 `BaseMapper<Entity>`。
*   添加 `@Mapper` 注解 (视项目配置而定，通常 Boot 项目在启动类扫描，这里可省略或加上)。

### C. Service (`service` 取 `service.impl` 包)
*   **Interface**: 定义基础 CRUD 方法。
*   **Implementation**:
    *   使用 `@Service`。
    *   使用 `@Slf4j`。
    *   继承 `ServiceImpl<Mapper, Entity>`。
    *   **依赖注入**: 必须使用 `@RequiredArgsConstructor` (final fields)。
    *   **事务**: 写入方法 (`add`, `update`, `delete`, `save`) 必须加 `@Transactional(rollbackFor = Exception.class)`。

### D. Controller (`controller` 包)
*   使用 `@RestController` 和 `@RequestMapping("/admin/模块路径")`。
*   **依赖注入**: 使用 `@RequiredArgsConstructor`。
*   **响应封装**: 所有方法返回 `Result<T>`。
*   **权限控制**: 在方法上添加 `@PreAuthorize("@ss.hasPermi('权限标识')")`。
    *   查询: `system:xxx:list`
    *   新增: `system:xxx:add`
    *   修改: `system:xxx:edit`
    *   删除: `system:xxx:remove`

## 3. 生成权限 SQL
在 `kh_permission` 表中生成对应的菜单和按钮数据。请输出 SQL `INSERT` 语句。

**数据结构参考**:
*   `name`: 菜单/按钮名称
*   `permission_key`: 权限标识 (e.g., `system:user:list`)
*   `type`: 1=菜单, 2=按钮
*   `path`: 路由路径 (e.g., `/system/user`)
*   `component`: 前端组件路径 (仅菜单需要, e.g., `system/User/index`)
*   `parent_id`: 父菜单 ID (询问用户或设为占位符)

**需要生成的记录**:
1.  **主菜单**: 类型为 1 (如果它是一个新页面)。
2.  **按钮权限**: 类型为 2 (查询, 新增, 修改, 删除)。

## 4. 最终检查
*   [ ] 检查是否有多余的 `public` 修饰符 (Interface 中)。
*   [ ] 检查是否使用了全限定类名 (应使用 Import)。
*   [ ] 检查代码格式是否整洁。
