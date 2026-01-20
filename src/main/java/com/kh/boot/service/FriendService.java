package com.kh.boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.dto.FriendDTO;
import com.kh.boot.dto.KhUserDTO;
import com.kh.boot.entity.KhFriend;

import java.util.List;

/**
 * 好友服务接口
 *
 * @author harlan
 * @since 2024-01-20
 */
public interface FriendService extends IService<KhFriend> {

    /**
     * 通过手机号或邮箱搜索用户
     *
     * @param keyword 手机号或邮箱
     * @return 用户列表
     */
    List<KhUserDTO> searchUser(String keyword);

    /**
     * 发送好友申请
     *
     * @param friendId 目标用户ID
     */
    void addFriend(String friendId);

    /**
     * 处理好友申请
     *
     * @param requestId 申请记录ID
     * @param accept    是否接受
     */
    void handleRequest(String requestId, boolean accept);

    /**
     * 获取好友列表（已同意的）
     *
     * @return 好友列表
     */
    List<FriendDTO> getFriends();

    /**
     * 获取待处理的好友请求（别人发给我的）
     *
     * @return 好友请求列表
     */
    List<FriendDTO> getPendingRequests();

    /**
     * 获取我发出的待处理请求
     *
     * @return 好友请求列表
     */
    List<FriendDTO> getSentRequests();

    /**
     * 删除好友
     *
     * @param friendId 好友ID
     */
    void deleteFriend(String friendId);

    /**
     * 检查是否为好友关系
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 是否为好友
     */
    boolean isFriend(String userId1, String userId2);
}
