package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.dto.FriendDTO;
import com.kh.boot.dto.KhUserDTO;
import com.kh.boot.entity.KhFriend;
import com.kh.boot.entity.KhUser;
import com.kh.boot.exception.BusinessException;
import com.kh.boot.mapper.FriendMapper;
import com.kh.boot.service.FriendService;
import com.kh.boot.service.UserService;
import com.kh.boot.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友服务实现
 *
 * @author harlan
 * @since 2024-01-20
 */
@Service
@RequiredArgsConstructor
public class FriendServiceImpl extends ServiceImpl<FriendMapper, KhFriend> implements FriendService {

    private final UserService userService;

    @Override
    public List<KhUserDTO> searchUser(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String currentUserId = SecurityUtils.getUserId();

        // 查找匹配手机号或邮箱的用户（排除自己）
        LambdaQueryWrapper<KhUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(KhUser::getPhone, keyword.trim())
                .or()
                .eq(KhUser::getEmail, keyword.trim())).ne(KhUser::getId, currentUserId);

        List<KhUser> users = userService.list(wrapper);

        return users.stream().map(user -> {
            KhUserDTO dto = new KhUserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFriend(String friendId) {
        String currentUserId = SecurityUtils.getUserId();

        if (currentUserId.equals(friendId)) {
            throw new BusinessException("不能添加自己为好友");
        }

        // 检查目标用户是否存在
        KhUser friend = userService.getById(friendId);
        if (friend == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查是否已经是好友或有待处理请求
        LambdaQueryWrapper<KhFriend> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.and(w -> w
                .eq(KhFriend::getUserId, currentUserId)
                .eq(KhFriend::getFriendId, friendId)).or(w -> w
                        .eq(KhFriend::getUserId, friendId)
                        .eq(KhFriend::getFriendId, currentUserId));

        KhFriend existing = this.getOne(existWrapper);
        if (existing != null) {
            if (existing.getStatus() == KhFriend.STATUS_ACCEPTED) {
                throw new BusinessException("你们已经是好友了");
            } else if (existing.getStatus() == KhFriend.STATUS_PENDING) {
                throw new BusinessException("已有待处理的好友请求");
            }
            // 如果之前被拒绝，可以重新发起
        }

        // 创建好友请求
        KhFriend friendRequest = new KhFriend();
        friendRequest.setUserId(currentUserId);
        friendRequest.setFriendId(friendId);
        friendRequest.setStatus(KhFriend.STATUS_PENDING);
        friendRequest.setCreateTime(new Date());
        friendRequest.setCreateBy(SecurityUtils.getUsername());

        this.save(friendRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRequest(String requestId, boolean accept) {
        String currentUserId = SecurityUtils.getUserId();

        KhFriend request = this.getById(requestId);
        if (request == null) {
            throw new BusinessException("请求不存在");
        }

        // 只有被添加方可以处理请求
        if (!request.getFriendId().equals(currentUserId)) {
            throw new BusinessException("无权处理此请求");
        }

        if (request.getStatus() != KhFriend.STATUS_PENDING) {
            throw new BusinessException("该请求已被处理");
        }

        request.setStatus(accept ? KhFriend.STATUS_ACCEPTED : KhFriend.STATUS_REJECTED);
        request.setUpdateTime(new Date());
        request.setUpdateBy(SecurityUtils.getUsername());

        this.updateById(request);

        // 如果接受，创建反向好友关系（双向好友）
        if (accept) {
            KhFriend reverse = new KhFriend();
            reverse.setUserId(currentUserId);
            reverse.setFriendId(request.getUserId());
            reverse.setStatus(KhFriend.STATUS_ACCEPTED);
            reverse.setCreateTime(new Date());
            reverse.setCreateBy(SecurityUtils.getUsername());
            this.save(reverse);
        }
    }

    @Override
    public List<FriendDTO> getFriends() {
        String currentUserId = SecurityUtils.getUserId();

        LambdaQueryWrapper<KhFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KhFriend::getUserId, currentUserId)
                .eq(KhFriend::getStatus, KhFriend.STATUS_ACCEPTED);

        List<KhFriend> friends = this.list(wrapper);

        return friends.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<FriendDTO> getPendingRequests() {
        String currentUserId = SecurityUtils.getUserId();

        // 别人发给我的待处理请求
        LambdaQueryWrapper<KhFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KhFriend::getFriendId, currentUserId)
                .eq(KhFriend::getStatus, KhFriend.STATUS_PENDING)
                .orderByDesc(KhFriend::getCreateTime);

        List<KhFriend> requests = this.list(wrapper);

        return requests.stream().map(this::toRequestDTO).collect(Collectors.toList());
    }

    @Override
    public List<FriendDTO> getSentRequests() {
        String currentUserId = SecurityUtils.getUserId();

        // 我发出的待处理请求
        LambdaQueryWrapper<KhFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KhFriend::getUserId, currentUserId)
                .eq(KhFriend::getStatus, KhFriend.STATUS_PENDING)
                .orderByDesc(KhFriend::getCreateTime);

        List<KhFriend> requests = this.list(wrapper);

        return requests.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(String friendId) {
        String currentUserId = SecurityUtils.getUserId();

        // 删除双向好友关系
        LambdaQueryWrapper<KhFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(KhFriend::getUserId, currentUserId)
                .eq(KhFriend::getFriendId, friendId)).or(w -> w
                        .eq(KhFriend::getUserId, friendId)
                        .eq(KhFriend::getFriendId, currentUserId));

        this.remove(wrapper);
    }

    @Override
    public boolean isFriend(String userId1, String userId2) {
        LambdaQueryWrapper<KhFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KhFriend::getUserId, userId1)
                .eq(KhFriend::getFriendId, userId2)
                .eq(KhFriend::getStatus, KhFriend.STATUS_ACCEPTED);

        return this.count(wrapper) > 0;
    }

    /**
     * 转换为 DTO（显示好友信息）
     */
    private FriendDTO toDTO(KhFriend friend) {
        FriendDTO dto = new FriendDTO();
        dto.setId(friend.getId());
        dto.setUserId(friend.getUserId());
        dto.setFriendId(friend.getFriendId());
        dto.setStatus(friend.getStatus());
        dto.setCreateTime(friend.getCreateTime());
        dto.setUpdateTime(friend.getUpdateTime());

        // 查询好友用户信息
        KhUser friendUser = userService.getById(friend.getFriendId());
        if (friendUser != null) {
            dto.setFriendUsername(friendUser.getUsername());
            dto.setFriendEmail(friendUser.getEmail());
            dto.setFriendPhone(friendUser.getPhone());
        }

        return dto;
    }

    /**
     * 转换为 DTO（显示申请人信息）
     */
    private FriendDTO toRequestDTO(KhFriend request) {
        FriendDTO dto = new FriendDTO();
        dto.setId(request.getId());
        dto.setUserId(request.getUserId());
        dto.setFriendId(request.getFriendId());
        dto.setStatus(request.getStatus());
        dto.setCreateTime(request.getCreateTime());
        dto.setUpdateTime(request.getUpdateTime());

        // 查询申请人用户信息
        KhUser requester = userService.getById(request.getUserId());
        if (requester != null) {
            dto.setFriendUsername(requester.getUsername());
            dto.setFriendEmail(requester.getEmail());
            dto.setFriendPhone(requester.getPhone());
        }

        return dto;
    }
}
