package com.kh.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.boot.entity.KhChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息 Mapper
 *
 * @author harlan
 * @since 2024-01-20
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<KhChatMessage> {
}
