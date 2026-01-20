CREATE TABLE `kh_config` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_by_name` varchar(64) DEFAULT NULL COMMENT '创建者姓名',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_by_name` varchar(64) DEFAULT NULL COMMENT '更新者姓名',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `version` int DEFAULT '0' COMMENT '乐观锁版本',
  `del_flag` int DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- 插入一些默认配置 (示例)
INSERT INTO `kh_config` (`id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_time`, `version`, `del_flag`) VALUES 
('1', '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', NOW(), 1, 0),
('2', '账号自助-是否开启用户注册', 'sys.account.registerUser', 'false', 'Y', NOW(), 1, 0),
('mail-host', '邮件服务器Host', 'sys.mail.host', 'smtp.163.com', 'Y', NOW(), 1, 0),
('mail-port', '邮件服务器Port', 'sys.mail.port', '465', 'Y', NOW(), 1, 0),
('mail-user', '邮件发送账号', 'sys.mail.username', '15225595010@163.com', 'Y', NOW(), 1, 0),
('mail-pass', '邮件授权码', 'sys.mail.password', 'XLNRHEZnUPGiRbYH', 'Y', NOW(), 1, 0),
('mail-proto', '邮件协议', 'sys.mail.protocol', 'smtp', 'Y', NOW(), 1, 0);
