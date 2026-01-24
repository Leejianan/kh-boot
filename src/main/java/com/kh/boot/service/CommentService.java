package com.kh.boot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.dto.CommentDTO;
import com.kh.boot.entity.FireComment;

/**
 * 评论服务接口
 *
 * @author harlan
 * @since 2026-01-23
 */
public interface CommentService extends IService<FireComment> {

    /**
     * 发表评论
     *
     * @param roomId   放映室ID
     * @param videoId  视频ID
     * @param content  内容
     * @param parentId 父评论ID（回复时使用）
     * @return 评论信息
     */
    CommentDTO addComment(String roomId, String videoId, String content, String parentId);

    /**
     * 分页获取评论列表
     *
     * @param videoId 视频ID
     * @param current 当前页
     * @param size    每页大小
     * @return 评论分页列表
     */
    IPage<CommentDTO> getCommentList(String videoId, int current, int size);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(String commentId);
}
