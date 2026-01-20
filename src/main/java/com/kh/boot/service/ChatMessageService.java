package com.kh.boot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.entity.KhChatMessage;

import java.util.List;

/**
 * 聊天消息服务接口
 *
 * @author harlan
 * @since 2024-01-20
 */
public interface ChatMessageService extends IService<KhChatMessage> {

    /**
     * 发送消息（保存到数据库 + 推送到接收方）
     *
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param msgType    消息类型
     * @return 保存的消息
     */
    KhChatMessage sendMessage(String receiverId, String content, Integer msgType);

    /**
     * 通过用户名发送消息（用于 WebSocket 上下文）
     *
     * @param senderUsername 发送者用户名
     * @param receiverId     接收者ID
     * @param content        消息内容
     * @param msgType        消息类型
     * @return 保存的消息
     */
    KhChatMessage sendMessageByUsername(String senderUsername, String receiverId, String content, Integer msgType);

    /**
     * 获取与指定好友的聊天记录
     *
     * @param friendId 好友ID
     * @param current  当前页
     * @param size     每页大小
     * @return 消息列表（按时间正序）
     */
    IPage<KhChatMessage> getHistory(String friendId, int current, int size);

    /**
     * 标记消息为已读
     *
     * @param messageIds 消息ID列表
     */
    void markAsRead(List<String> messageIds);

    /**
     * 标记与某好友的所有消息为已读
     *
     * @param friendId 好友ID
     */
    void markAllAsRead(String friendId);

    /**
     * 通过用户名标记与某好友的所有消息为已读（用于 WebSocket 上下文）
     *
     * @param username 当前用户用户名
     * @param friendId 好友ID
     */
    void markAllAsReadByUsername(String username, String friendId);

    /**
     * 获取未读消息数
     *
     * @return 未读消息总数
     */
    int getUnreadCount();

    /**
     * 获取与指定好友的未读消息数
     *
     * @param friendId 好友ID
     * @return 未读消息数
     */
    int getUnreadCountByFriend(String friendId);
}
