package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.entity.KhChatMessage;
import com.kh.boot.mapper.ChatMessageMapper;
import com.kh.boot.service.ChatMessageService;
import com.kh.boot.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 聊天消息服务实现
 *
 * @author harlan
 * @since 2024-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, KhChatMessage>
        implements ChatMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final com.kh.boot.service.UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KhChatMessage sendMessage(String receiverId, String content, Integer msgType) {
        String senderId = SecurityUtils.getUserId();

        // 创建消息
        KhChatMessage message = new KhChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMsgType(msgType != null ? msgType : KhChatMessage.TYPE_TEXT);
        message.setIsRead(0);
        message.setCreateTime(new Date());
        message.setCreateBy(SecurityUtils.getUsername());

        // 保存到数据库
        this.save(message);

        // 通过 WebSocket 推送给接收方
        // convertAndSendToUser 需要使用 Principal name (即 username)
        com.kh.boot.entity.KhUser receiver = userService.getById(receiverId);
        if (receiver != null) {
            messagingTemplate.convertAndSendToUser(
                    receiver.getUsername(), // 使用 username 而不是 userId
                    "/queue/chat",
                    message);
        }

        return message;
    }

    @Override
    public IPage<KhChatMessage> getHistory(String friendId, int current, int size) {
        String currentUserId = SecurityUtils.getUserId();

        LambdaQueryWrapper<KhChatMessage> wrapper = new LambdaQueryWrapper<>();
        // 查询双方的消息
        wrapper.and(w -> w
                .and(sub -> sub.eq(KhChatMessage::getSenderId, currentUserId).eq(KhChatMessage::getReceiverId,
                        friendId))
                .or(sub -> sub.eq(KhChatMessage::getSenderId, friendId).eq(KhChatMessage::getReceiverId,
                        currentUserId)))
                .orderByAsc(KhChatMessage::getCreateTime);

        return this.page(new Page<>(current, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        String currentUserId = SecurityUtils.getUserId();

        LambdaUpdateWrapper<KhChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(KhChatMessage::getId, messageIds)
                .eq(KhChatMessage::getReceiverId, currentUserId) // 只能标记发给自己的消息
                .eq(KhChatMessage::getIsRead, 0)
                .set(KhChatMessage::getIsRead, 1)
                .set(KhChatMessage::getUpdateTime, new Date());

        this.update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(String friendId) {
        String currentUserId = SecurityUtils.getUserId();

        LambdaUpdateWrapper<KhChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(KhChatMessage::getSenderId, friendId)
                .eq(KhChatMessage::getReceiverId, currentUserId)
                .eq(KhChatMessage::getIsRead, 0)
                .set(KhChatMessage::getIsRead, 1)
                .set(KhChatMessage::getUpdateTime, new Date());

        this.update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KhChatMessage sendMessageByUsername(String senderUsername, String receiverId, String content,
            Integer msgType) {
        // 通过用户名查找发送者
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.kh.boot.entity.KhUser> senderWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        senderWrapper.eq(com.kh.boot.entity.KhUser::getUsername, senderUsername);
        com.kh.boot.entity.KhUser sender = userService.getOne(senderWrapper);
        if (sender == null) {
            throw new RuntimeException("发送者不存在: " + senderUsername);
        }

        // 创建消息
        KhChatMessage message = new KhChatMessage();
        message.setSenderId(sender.getId());
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMsgType(msgType != null ? msgType : KhChatMessage.TYPE_TEXT);
        message.setIsRead(0);
        message.setCreateTime(new Date());
        message.setCreateBy(senderUsername);

        // 保存到数据库
        log.info("Saving chat message to DB: from {} to {}", senderUsername, receiverId);
        this.save(message);

        // 通过 WebSocket 推送给接收方
        com.kh.boot.entity.KhUser receiver = userService.getById(receiverId);
        if (receiver != null) {
            log.info("Pushing message to receiver username: {}", receiver.getUsername());
            messagingTemplate.convertAndSendToUser(
                    receiver.getUsername(),
                    "/queue/chat",
                    message);
        } else {
            log.warn("Receiver not found for ID: {}", receiverId);
        }

        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsReadByUsername(String username, String friendId) {
        // 通过用户名查找当前用户
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.kh.boot.entity.KhUser> userWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        userWrapper.eq(com.kh.boot.entity.KhUser::getUsername, username);
        com.kh.boot.entity.KhUser currentUser = userService.getOne(userWrapper);
        if (currentUser == null) {
            return;
        }

        LambdaUpdateWrapper<KhChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(KhChatMessage::getSenderId, friendId)
                .eq(KhChatMessage::getReceiverId, currentUser.getId())
                .eq(KhChatMessage::getIsRead, 0)
                .set(KhChatMessage::getIsRead, 1)
                .set(KhChatMessage::getUpdateTime, new Date());

        this.update(wrapper);
    }

    @Override
    public int getUnreadCount() {
        String currentUserId = SecurityUtils.getUserId();

        LambdaQueryWrapper<KhChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KhChatMessage::getReceiverId, currentUserId)
                .eq(KhChatMessage::getIsRead, 0);

        return (int) this.count(wrapper);
    }

    @Override
    public int getUnreadCountByFriend(String friendId) {
        String currentUserId = SecurityUtils.getUserId();

        LambdaQueryWrapper<KhChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KhChatMessage::getSenderId, friendId)
                .eq(KhChatMessage::getReceiverId, currentUserId)
                .eq(KhChatMessage::getIsRead, 0);

        return (int) this.count(wrapper);
    }
}
