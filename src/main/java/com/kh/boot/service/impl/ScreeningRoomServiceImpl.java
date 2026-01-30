package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.dto.ScreeningRoomDTO;
import com.kh.boot.entity.FireRoomMember;
import com.kh.boot.entity.FireScreeningRoom;
import com.kh.boot.entity.FireVideo;
import com.kh.boot.entity.KhUser;
import com.kh.boot.exception.BusinessException;
import com.kh.boot.mapper.FireRoomMemberMapper;
import com.kh.boot.mapper.FireScreeningRoomMapper;
import com.kh.boot.mapper.FireVideoMapper;
import com.kh.boot.service.ScreeningRoomService;
import com.kh.boot.service.UserService;
import com.kh.boot.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 放映室服务实现
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningRoomServiceImpl extends ServiceImpl<FireScreeningRoomMapper, FireScreeningRoom>
        implements ScreeningRoomService {

    private final FireRoomMemberMapper roomMemberMapper;
    private final FireVideoMapper videoMapper;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public IPage<ScreeningRoomDTO> getRoomList(int current, int size, String name) {
        LambdaQueryWrapper<FireScreeningRoom> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(FireScreeningRoom::getName, name);
        }
        wrapper.eq(FireScreeningRoom::getStatus, 1)
                .orderByDesc(FireScreeningRoom::getCreateTime);

        IPage<FireScreeningRoom> roomPage = this.page(new Page<>(current, size), wrapper);

        // 转换为 DTO
        return roomPage.convert(this::convertToDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FireScreeningRoom createRoom(String name, String password) {
        String userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();

        FireScreeningRoom room = new FireScreeningRoom();
        room.setName(name);
        room.setOwnerId(userId);
        room.setPassword(password);
        room.setPlayTime(0);
        room.setIsPlaying(0);
        room.setStatus(1);
        room.setCreateBy(userId);
        room.setCreateByName(username);
        room.setCreateTime(new Date());

        this.save(room);

        // 房主自动加入房间
        joinRoomInternal(room.getId(), userId);

        log.info("User {} created room: {} ({})", username, name, room.getId());
        return room;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinRoom(String roomId, String password) {
        String userId = SecurityUtils.getUserId();

        FireScreeningRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("放映室不存在");
        }

        if (room.getStatus() != 1) {
            throw new BusinessException("放映室已关闭");
        }

        // 检查是否已在房间中（未被删除的记录）
        // 如果用户已经是成员，直接通过，无需再验证密码
        LambdaQueryWrapper<FireRoomMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FireRoomMember::getRoomId, roomId)
                .eq(FireRoomMember::getUserId, userId);
        if (roomMemberMapper.selectCount(memberWrapper) > 0) {
            return; // 已在房间中
        }

        // 验证密码（房主无需验证，直接放行）
        // 如果曾经加入过（包括异常离线导致逻辑删除的），也无需验证
        boolean isOwner = room.getOwnerId().equals(userId);
        boolean hasJoinedBefore = roomMemberMapper.countIncludingDeleted(roomId, userId) > 0;

        if (!isOwner && !hasJoinedBefore && StringUtils.hasText(room.getPassword())) {
            if (!room.getPassword().equals(password)) {
                throw new BusinessException("密码错误");
            }
        }

        // 尝试恢复已删除的记录（物理查询，绕过逻辑删除）
        // 这里采用：先物理删除旧记录，再插入新记录
        log.info("Attempting physical delete of member records for room {} and user {}", roomId, userId);
        int deletedCount = roomMemberMapper.delete(new LambdaQueryWrapper<FireRoomMember>()
                .eq(FireRoomMember::getRoomId, roomId)
                .eq(FireRoomMember::getUserId, userId)
                .apply("1=1")); // 强制物理删除，包括已逻辑删除的
        log.info("Deleted {} existing member records", deletedCount);

        try {
            joinRoomInternal(roomId, userId);
            log.info("Successfully joined roomInternal for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to joinRoomInternal for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }

        // 通知房间内其他成员
        notifyRoomMembers(roomId, "member_join", SecurityUtils.getUsername() + " 加入了放映室");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinRoom(String roomId) {
        String userId = SecurityUtils.getUserId();

        FireScreeningRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("放映室不存在");
        }

        if (room.getStatus() != 1) {
            throw new BusinessException("放映室已关闭");
        }

        // 无需验证密码

        // 检查是否已在房间中（未被删除的记录）
        LambdaQueryWrapper<FireRoomMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FireRoomMember::getRoomId, roomId)
                .eq(FireRoomMember::getUserId, userId);
        if (roomMemberMapper.selectCount(memberWrapper) > 0) {
            return; // 已在房间中
        }

        // 尝试恢复已删除的记录（物理查询，绕过逻辑删除）
        // 这里采用：先物理删除旧记录，再插入新记录
        log.info("Attempting physical delete of member records for room {} and user {}", roomId, userId);
        int deletedCount = roomMemberMapper.delete(new LambdaQueryWrapper<FireRoomMember>()
                .eq(FireRoomMember::getRoomId, roomId)
                .eq(FireRoomMember::getUserId, userId)
                .apply("1=1")); // 强制物理删除，包括已逻辑删除的
        log.info("Deleted {} existing member records", deletedCount);

        try {
            joinRoomInternal(roomId, userId);
            log.info("Successfully joined roomInternal for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to joinRoomInternal for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }

        // 通知房间内其他成员
        notifyRoomMembers(roomId, "member_join", SecurityUtils.getUsername() + " 加入了放映室");
    }

    private void joinRoomInternal(String roomId, String userId) {
        FireRoomMember member = new FireRoomMember();
        member.setRoomId(roomId);
        member.setUserId(userId);
        member.setJoinTime(new Date());
        member.setCreateTime(new Date());
        member.setDelFlag(0); // 确保 delFlag 是 0
        roomMemberMapper.insert(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveRoom(String roomId) {
        String userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();

        // 检查是否是房主
        FireScreeningRoom room = this.getById(roomId);
        boolean isOwner = room != null && room.getOwnerId().equals(userId);

        LambdaQueryWrapper<FireRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireRoomMember::getRoomId, roomId)
                .eq(FireRoomMember::getUserId, userId);
        roomMemberMapper.delete(wrapper);

        // 通知房间内其他成员（包含 isOwner 信息）
        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                java.util.Map.of(
                        "type", "member_leave",
                        "username", username,
                        "isOwner", isOwner,
                        "message", username + " 离开了放映室",
                        "timestamp", System.currentTimeMillis()));

        log.info("User {} left room: {} (isOwner: {})", username, roomId, isOwner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchVideo(String roomId, String videoId) {
        String userId = SecurityUtils.getUserId();

        FireScreeningRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("放映室不存在");
        }

        // 只有房主可以切换视频
        if (!room.getOwnerId().equals(userId)) {
            throw new BusinessException("只有房主可以切换视频");
        }

        FireVideo video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }

        room.setCurrentVideoId(videoId);
        room.setPlayTime(0);
        room.setIsPlaying(0);
        room.setUpdateTime(new Date());
        this.updateById(room);

        // 通知房间内成员
        notifyRoomMembers(roomId, "video_switch", video.getTitle());

        log.info("Room {} switched to video: {}", roomId, video.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchVideoInternal(String roomId, String videoId) {
        FireScreeningRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("放映室不存在");
        }

        FireVideo video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }

        room.setCurrentVideoId(videoId);
        room.setPlayTime(0);
        room.setIsPlaying(0);
        room.setUpdateTime(new Date());
        this.updateById(room);

        log.info("Room {} switched to video (internal): {}", roomId, video.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncPlayStatus(String roomId, boolean isPlaying, int currentTime) {
        FireScreeningRoom room = this.getById(roomId);
        if (room == null) {
            return;
        }

        room.setIsPlaying(isPlaying ? 1 : 0);
        room.setPlayTime(currentTime);
        room.setUpdateTime(new Date());
        this.updateById(room);
    }

    @Override
    public ScreeningRoomDTO getRoomDetail(String roomId) {
        FireScreeningRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("放映室不存在");
        }

        ScreeningRoomDTO dto = convertToDTO(room);

        // 获取成员列表
        LambdaQueryWrapper<FireRoomMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FireRoomMember::getRoomId, roomId);
        List<FireRoomMember> members = roomMemberMapper.selectList(memberWrapper);

        List<ScreeningRoomDTO.RoomMemberDTO> memberDTOList = new ArrayList<>();
        for (FireRoomMember member : members) {
            KhUser user = userService.getById(member.getUserId());
            if (user != null) {
                ScreeningRoomDTO.RoomMemberDTO memberDTO = new ScreeningRoomDTO.RoomMemberDTO();
                memberDTO.setUserId(user.getId());
                memberDTO.setUsername(user.getUsername());
                memberDTO.setRealName(user.getRealName());
                memberDTO.setAvatar(user.getAvatar());
                memberDTO.setJoinTime(member.getJoinTime());
                memberDTOList.add(memberDTO);
            }
        }
        dto.setMembers(memberDTOList);

        return dto;
    }

    @Override
    public List<String> getRoomMemberUsernames(String roomId) {
        LambdaQueryWrapper<FireRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireRoomMember::getRoomId, roomId);
        List<FireRoomMember> members = roomMemberMapper.selectList(wrapper);

        return members.stream()
                .map(member -> {
                    KhUser user = userService.getById(member.getUserId());
                    return user != null ? user.getUsername() : null;
                })
                .filter(username -> username != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeRoom(String roomId) {
        String userId = SecurityUtils.getUserId();

        FireScreeningRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("放映室不存在");
        }

        if (!room.getOwnerId().equals(userId)) {
            throw new BusinessException("只有房主可以关闭放映室");
        }

        room.setStatus(0);
        room.setUpdateTime(new Date());
        this.updateById(room);

        // 通知房间内成员
        notifyRoomMembers(roomId, "room_closed", "放映室已关闭");

        // 清空成员
        LambdaQueryWrapper<FireRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireRoomMember::getRoomId, roomId);
        roomMemberMapper.delete(wrapper);

        log.info("Room {} closed by owner", roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleUserOffline(String userId) {
        // 查找该用户所在的房间（理论上同时只能在一个房间）
        LambdaQueryWrapper<FireRoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireRoomMember::getUserId, userId);
        List<FireRoomMember> members = roomMemberMapper.selectList(wrapper);

        for (FireRoomMember member : members) {
            String roomId = member.getRoomId();

            // 删除成员记录
            roomMemberMapper.deleteById(member.getId());

            // 获取用户信息用于通知
            KhUser user = userService.getById(userId);
            String username = (user != null) ? user.getUsername() : "Unknown";

            // 检查是不是房主
            FireScreeningRoom room = this.getById(roomId);
            boolean isOwner = (room != null && room.getOwnerId().equals(userId));

            // 如果是房主离线，暂停播放
            if (isOwner && room != null && room.getIsPlaying() == 1) {
                room.setIsPlaying(0);
                room.setUpdateTime(new Date());
                this.updateById(room);
                // 通知停止播放
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/sync",
                        java.util.Map.of("type", "sync", "isPlaying", false, "currentTime", room.getPlayTime(),
                                "sender", "system", "timestamp", System.currentTimeMillis()));
            }

            // 通知房间内其他成员
            messagingTemplate.convertAndSend("/topic/room/" + roomId,
                    java.util.Map.of(
                            "type", "member_leave",
                            "username", username,
                            "isOwner", isOwner,
                            "message", isOwner ? "房主已离线，播放已暂停" : username + " 离开了放映室",
                            "timestamp", System.currentTimeMillis()));

            log.info("User {} (offline) removed from room {}", username, roomId);
        }
    }

    private ScreeningRoomDTO convertToDTO(FireScreeningRoom room) {
        ScreeningRoomDTO dto = new ScreeningRoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setOwnerId(room.getOwnerId());
        dto.setCurrentVideoId(room.getCurrentVideoId());
        dto.setCurrentTime(room.getPlayTime());
        dto.setIsPlaying(room.getIsPlaying());
        dto.setHasPassword(StringUtils.hasText(room.getPassword()));
        dto.setStatus(room.getStatus());
        dto.setCreateTime(room.getCreateTime());

        // 获取房主信息
        KhUser owner = userService.getById(room.getOwnerId());
        if (owner != null) {
            dto.setOwnerName(owner.getRealName() != null ? owner.getRealName() : owner.getUsername());
            dto.setOwnerAvatar(owner.getAvatar());
            dto.setOwnerUsername(owner.getUsername());
        }

        // 获取当前视频信息
        log.debug("convertToDTO: Checking currentVideoId for room {}. CurrentVideoId: {}", room.getId(),
                room.getCurrentVideoId());
        if (StringUtils.hasText(room.getCurrentVideoId())) {
            FireVideo video = videoMapper.selectById(room.getCurrentVideoId());
            if (video == null) {
                log.warn("convertToDTO: Video not found for currentVideoId {} in room {}", room.getCurrentVideoId(),
                        room.getId());
            } else {
                log.debug("convertToDTO: Found video {} for currentVideoId {} in room {}", video.getTitle(),
                        room.getCurrentVideoId(), room.getId());
                dto.setCurrentVideoTitle(video.getTitle());
                // 返回视频流 URL 而非本地文件路径
                dto.setCurrentVideoUrl("/admin/video/stream/" + video.getId());
                log.debug("convertToDTO: currentVideoUrl set to {}", dto.getCurrentVideoUrl());
            }
        }

        // 获取成员数量
        LambdaQueryWrapper<FireRoomMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FireRoomMember::getRoomId, room.getId());
        dto.setMemberCount(Long.valueOf(roomMemberMapper.selectCount(memberWrapper)).intValue());

        // 判断当前用户是否是历史成员（用于前端免密判断）
        try {
            String currentUserId = SecurityUtils.getUserId();
            if (currentUserId != null) {
                boolean isOwner = room.getOwnerId().equals(currentUserId);
                boolean hasJoined = roomMemberMapper.countIncludingDeleted(room.getId(), currentUserId) > 0;
                dto.setIsHistoryMember(isOwner || hasJoined);
            } else {
                dto.setIsHistoryMember(false);
            }
        } catch (Exception e) {
            // 忽略未登录异常
            dto.setIsHistoryMember(false);
        }

        return dto;
    }

    private void notifyRoomMembers(String roomId, String type, String message) {
        // 广播到房间频道
        messagingTemplate.convertAndSend("/topic/room/" + roomId,
                java.util.Map.of("type", type, "message", message, "timestamp", System.currentTimeMillis()));
    }
}
