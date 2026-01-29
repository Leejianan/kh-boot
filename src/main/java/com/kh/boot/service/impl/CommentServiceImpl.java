package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.dto.CommentDTO;
import com.kh.boot.entity.FireComment;
import com.kh.boot.entity.KhUser;
import com.kh.boot.exception.BusinessException;
import com.kh.boot.mapper.FireCommentMapper;
import com.kh.boot.service.CommentService;
import com.kh.boot.service.UserService;
import com.kh.boot.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<FireCommentMapper, FireComment>
        implements CommentService {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentDTO addComment(String roomId, String videoId, String content, String parentId) {
        String userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();

        FireComment comment = new FireComment();
        comment.setRoomId(roomId);
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setCreateTime(new Date());
        comment.setCreateBy(userId);
        comment.setCreateByName(username);

        this.save(comment);

        CommentDTO dto = convertToDTO(comment);

        // 通过 WebSocket 广播新评论到房间内的所有成员
        if (roomId != null) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/comment", dto);
            log.info("Broadcasted new comment to room {}: {}", roomId, content);
        }

        return dto;
    }

    @Override
    public IPage<CommentDTO> getCommentList(String videoId, int current, int size) {
        LambdaQueryWrapper<FireComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireComment::getVideoId, videoId)
                .isNull(FireComment::getParentId) // 只查询一级评论
                .orderByDesc(FireComment::getCreateTime);

        IPage<FireComment> commentPage = this.page(new Page<>(current, size), wrapper);

        return commentPage.convert(comment -> {
            CommentDTO dto = convertToDTO(comment);
            // 获取子评论
            dto.setReplies(getReplies(comment.getId()));
            return dto;
        });
    }

    @Override
    public IPage<CommentDTO> getCommentListByRoom(String roomId, int current, int size) {
        LambdaQueryWrapper<FireComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireComment::getRoomId, roomId)
                .isNull(FireComment::getParentId) // 只查询一级评论
                .orderByDesc(FireComment::getCreateTime);

        IPage<FireComment> commentPage = this.page(new Page<>(current, size), wrapper);

        return commentPage.convert(comment -> {
            CommentDTO dto = convertToDTO(comment);
            // 获取子评论
            dto.setReplies(getReplies(comment.getId()));
            return dto;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(String commentId) {
        String userId = SecurityUtils.getUserId();

        FireComment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }

        // 删除评论及其回复
        LambdaQueryWrapper<FireComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireComment::getParentId, commentId);
        this.remove(wrapper);

        this.removeById(commentId);
    }

    private List<CommentDTO> getReplies(String parentId) {
        LambdaQueryWrapper<FireComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireComment::getParentId, parentId)
                .orderByAsc(FireComment::getCreateTime);

        List<FireComment> replies = this.list(wrapper);
        return replies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CommentDTO convertToDTO(FireComment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setRoomId(comment.getRoomId());
        dto.setVideoId(comment.getVideoId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setParentId(comment.getParentId());
        dto.setCreateTime(comment.getCreateTime());

        // 获取用户信息
        KhUser user = userService.getById(comment.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setRealName(user.getRealName());
            dto.setAvatar(user.getAvatar());
        }

        return dto;
    }
}
