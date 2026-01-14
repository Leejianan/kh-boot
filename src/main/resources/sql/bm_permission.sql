-- 权限表
CREATE TABLE `bm_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) DEFAULT NULL COMMENT '权限名称',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父权限ID',
  `permission_key` varchar(64) DEFAULT NULL COMMENT '权限标识符',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型(0:目录, 1:菜单, 2:按钮)',
  `path` varchar(128) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(128) DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(64) DEFAULT NULL COMMENT '图标',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态(1:正常, 0:禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 初始化示例数据
INSERT INTO `bm_permission` (`id`, `name`, `parent_id`, `permission_key`, `type`, `path`, `sort`, `status`) VALUES
(1, '系统管理', 0, '', 0, '/system', 1, 1),
(2, '用户管理', 1, 'system:user:list', 1, '/system/user', 1, 1),
(3, '用户新增', 2, 'system:user:add', 2, '', 1, 1),
(4, '用户修改', 2, 'system:user:edit', 2, '', 2, 1),
(5, '用户删除', 2, 'system:user:delete', 2, '', 3, 1),
(6, '商品管理', 0, '', 0, '/items', 2, 1),
(7, '商品列表', 6, 'item:list', 1, '/items/list', 1, 1);
