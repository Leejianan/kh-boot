-- Create database
CREATE DATABASE IF NOT EXISTS boom CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE boom;

-- 1. Permission Table
CREATE TABLE IF NOT EXISTS `kh_permission` (
  `id` varchar(64) NOT NULL COMMENT 'Primary Key ID',
  `name` varchar(50) NOT NULL COMMENT 'Permission Name',
  `parent_id` varchar(64) DEFAULT '0' COMMENT 'Parent ID',
  `permission_key` varchar(100) DEFAULT NULL COMMENT 'Permission Key (e.g. system:user:add)',
  `type` tinyint(2) NOT NULL DEFAULT '0' COMMENT 'Resource Type (0:Directory, 1:Menu, 2:Button)',
  `path` varchar(255) DEFAULT NULL COMMENT 'Router Path',
  `component` varchar(255) DEFAULT NULL COMMENT 'Component Path',
  `icon` varchar(100) DEFAULT NULL COMMENT 'Icon',
  `sort` int(11) DEFAULT '0' COMMENT 'Sort Order',
  `status` tinyint(1) DEFAULT '1' COMMENT 'Status (1:Normal, 0:Disabled)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `create_by` varchar(64) DEFAULT NULL COMMENT 'Creator ID',
  `create_by_name` varchar(64) DEFAULT NULL COMMENT 'Creator Name',
  `update_by` varchar(64) DEFAULT NULL COMMENT 'Updater ID',
  `update_by_name` varchar(64) DEFAULT NULL COMMENT 'Updater Name',
  `version` int(11) DEFAULT '1' COMMENT 'Optimistic Lock Version',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT 'Logic Delete (0: Normal, 1: Deleted)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Permission Table';

-- 2. Role Table
CREATE TABLE IF NOT EXISTS `kh_role` (
  `id` varchar(64) NOT NULL COMMENT 'Primary Key ID',
  `name` varchar(50) NOT NULL COMMENT 'Role Name',
  `role_key` varchar(50) NOT NULL COMMENT 'Role Key (e.g. admin)',
  `sort` int(11) DEFAULT '0' COMMENT 'Sort Order',
  `status` tinyint(1) DEFAULT '1' COMMENT 'Status (1:Normal, 0:Disabled)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `create_by` varchar(64) DEFAULT NULL COMMENT 'Creator ID',
  `create_by_name` varchar(64) DEFAULT NULL COMMENT 'Creator Name',
  `update_by` varchar(64) DEFAULT NULL COMMENT 'Updater ID',
  `update_by_name` varchar(64) DEFAULT NULL COMMENT 'Updater Name',
  `version` int(11) DEFAULT '1' COMMENT 'Optimistic Lock Version',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT 'Logic Delete (0: Normal, 1: Deleted)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role Table';

-- 3. User Table
CREATE TABLE IF NOT EXISTS `kh_user` (
  `id` varchar(64) NOT NULL COMMENT 'User ID',
  `username` varchar(50) NOT NULL COMMENT 'Username',
  `password` varchar(100) NOT NULL COMMENT 'Password',
  `user_code` varchar(50) DEFAULT NULL COMMENT 'Business User Code',
  `phone` varchar(20) DEFAULT NULL COMMENT 'Phone Number',
  `email` varchar(50) DEFAULT NULL COMMENT 'Email',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'User Avatar URL',
  `status` tinyint(1) DEFAULT '1' COMMENT 'Status: 1-Normal, 0-Disabled',
  `audit_status` tinyint(1) DEFAULT '1' COMMENT 'Audit Status: 0-Pending, 1-Approved, 2-Rejected',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `audit_time` datetime DEFAULT NULL COMMENT 'Audit Time',
  `auditor` varchar(50) DEFAULT NULL COMMENT 'Auditor User',
  `create_by` varchar(64) DEFAULT NULL COMMENT 'Creator ID',
  `create_by_name` varchar(64) DEFAULT NULL COMMENT 'Creator Name',
  `update_by` varchar(64) DEFAULT NULL COMMENT 'Updater ID',
  `update_by_name` varchar(64) DEFAULT NULL COMMENT 'Updater Name',
  `version` int(11) DEFAULT '1' COMMENT 'Optimistic Lock Version',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT 'Logic Delete (0: Normal, 1: Deleted)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  UNIQUE KEY `idx_user_code` (`user_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Table';

-- 4. User-Role Association
CREATE TABLE IF NOT EXISTS `kh_user_role` (
  `id` varchar(64) NOT NULL,
  `user_id` varchar(64) NOT NULL COMMENT 'User ID',
  `role_id` varchar(64) NOT NULL COMMENT 'Role ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Role Association';

-- 5. Role-Permission Association
CREATE TABLE IF NOT EXISTS `kh_role_permission` (
  `id` varchar(64) NOT NULL,
  `role_id` varchar(64) NOT NULL COMMENT 'Role ID',
  `permission_id` varchar(64) NOT NULL COMMENT 'Permission ID',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role Permission Association';

-- 6. Serial Number Table
CREATE TABLE IF NOT EXISTS `kh_serial_number` (
  `id` varchar(64) NOT NULL COMMENT 'Primary Key ID',
  `business_key` varchar(50) NOT NULL COMMENT 'Business Key (e.g. USER_CODE)',
  `prefix` varchar(20) DEFAULT NULL COMMENT 'Historical Prefix (Optional)',
  `date_part` varchar(20) DEFAULT NULL COMMENT 'Current Date Part (e.g. 20240114)',
  `current_value` bigint(20) NOT NULL DEFAULT '0' COMMENT 'Current Max Value',
  `rule_prefix` varchar(20) DEFAULT NULL COMMENT 'Configured Prefix',
  `rule_date_format` varchar(20) DEFAULT NULL COMMENT 'Configured Date Format',
  `rule_width` int(11) DEFAULT NULL COMMENT 'Configured Sequence Width',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_business_key` (`business_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Serial Number Generator Table with Dynamic Rules';

-- Initial Data (Snowflake IDs for demo, in reality these would be generated)
-- Admin User: password is '123456' (BCrypt hashed)
INSERT IGNORE INTO `kh_user` (`id`, `username`, `password`, `status`, `avatar`) VALUES ('1', 'admin', '$2a$10$M6iLB/KkSxtNz0ITT6WTaOXc53B5J9VHklmkTIK3dyQmHHVq5396W', 1, 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif');

-- Admin Role
INSERT IGNORE INTO `kh_role` (`id`, `name`, `role_key`, `sort`) VALUES ('1', 'Administrator', 'admin', 1);

-- User-Role Link
INSERT IGNORE INTO `kh_user_role` (`id`, `user_id`, `role_id`) VALUES ('1', '1', '1');

-- Permissions demo
INSERT IGNORE INTO `kh_permission` (`id`, `name`, `parent_id`, `permission_key`, `type`, `sort`) VALUES 
('1', '系统管理', '0', 'system', 0, 1),
('2', '用户管理', '1', 'system:user', 1, 1),
('3', '用户列表', '2', 'system:user:list', 2, 1),
('4', '角色管理', '1', 'system:role', 1, 2),
('5', '角色列表', '4', 'system:role:list', 2, 1),
('6', '权限管理', '1', 'system:permission', 1, 3),
('7', '权限列表', '6', 'system:permission:list', 2, 1),
('8', '在线用户', '1', 'system:online', 1, 4),
('9', '在线列表', '8', 'system:online:list', 2, 1);

-- 7. Email Record Table
CREATE TABLE IF NOT EXISTS `kh_email_record` (
  `id` varchar(64) NOT NULL COMMENT 'Primary Key ID',
  `send_to` varchar(255) DEFAULT NULL COMMENT 'Recipient Email',
  `send_subject` varchar(255) DEFAULT NULL COMMENT 'Email Subject',
  `send_content` text COMMENT 'Email Content',
  `send_result` tinyint(2) DEFAULT NULL COMMENT 'Sending Result: 1-Success, 0-Fail',
  `fail_reason` text COMMENT 'Failure Reason',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `create_by` varchar(64) DEFAULT NULL COMMENT 'Creator ID',
  `create_by_name` varchar(64) DEFAULT NULL COMMENT 'Creator Name',
  `update_by` varchar(64) DEFAULT NULL COMMENT 'Updater ID',
  `update_by_name` varchar(64) DEFAULT NULL COMMENT 'Updater Name',
  `version` int(11) DEFAULT '1' COMMENT 'Optimistic Lock Version',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT 'Logic Delete (0: Normal, 1: Deleted)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Email Sending Record';

