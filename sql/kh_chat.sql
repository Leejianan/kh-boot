-- ============================================
-- 好友关系表
-- ============================================
CREATE TABLE IF NOT EXISTS `kh_friend` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `user_id` VARCHAR(32) NOT NULL COMMENT '发起方用户ID',
    `friend_id` VARCHAR(32) NOT NULL COMMENT '被添加方用户ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待确认, 1-已同意, 2-已拒绝',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者ID',
    `create_by_name` VARCHAR(64) DEFAULT '' COMMENT '创建者名称',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者ID',
    `update_by_name` VARCHAR(64) DEFAULT '' COMMENT '更新者名称',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    `del_flag` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:正常, 1:已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_friend_id` (`friend_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- ============================================
-- 聊天消息表
-- ============================================
CREATE TABLE IF NOT EXISTS `kh_chat_message` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
    `sender_id` VARCHAR(32) NOT NULL COMMENT '发送者ID',
    `receiver_id` VARCHAR(32) NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `msg_type` TINYINT NOT NULL DEFAULT 0 COMMENT '消息类型: 0-文本, 1-图片, 2-文件',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者ID',
    `create_by_name` VARCHAR(64) DEFAULT '' COMMENT '创建者名称',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者ID',
    `update_by_name` VARCHAR(64) DEFAULT '' COMMENT '更新者名称',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    `del_flag` TINYINT DEFAULT 0 COMMENT '逻辑删除(0:正常, 1:已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';
