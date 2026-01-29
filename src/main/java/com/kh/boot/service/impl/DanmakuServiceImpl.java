package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.dto.DanmakuDTO;
import com.kh.boot.entity.FireDanmaku;
import com.kh.boot.entity.KhUser;
import com.kh.boot.mapper.FireDanmakuMapper;
import com.kh.boot.service.DanmakuService;
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
 * 弹幕服务实现
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DanmakuServiceImpl extends ServiceImpl<FireDanmakuMapper, FireDanmaku>
        implements DanmakuService {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DanmakuDTO sendDanmaku(String userId, String username, String roomId, String videoId, String content,
            int videoTime,
            String color, String position) {

        FireDanmaku danmaku = new FireDanmaku();
        danmaku.setRoomId(roomId);
        danmaku.setVideoId(videoId);
        danmaku.setUserId(userId);
        danmaku.setContent(content);
        danmaku.setVideoTime(videoTime);
        danmaku.setColor(color != null ? color : "#FFFFFF");
        danmaku.setPosition(position != null ? position : "scroll");
        danmaku.setCreateTime(new Date());
        danmaku.setCreateBy(userId);
        danmaku.setCreateByName(username);

        this.save(danmaku);

        DanmakuDTO dto = convertToDTO(danmaku);

        // 广播弹幕到房间
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/danmaku", dto);

        return dto;
    }

    @Override
    public List<DanmakuDTO> getDanmakuList(String videoId) {
        LambdaQueryWrapper<FireDanmaku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireDanmaku::getVideoId, videoId)
                .orderByAsc(FireDanmaku::getVideoTime);

        List<FireDanmaku> danmakuList = this.list(wrapper);
        return danmakuList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DanmakuDTO> getDanmakuByTimeRange(String videoId, int startTime, int endTime) {
        LambdaQueryWrapper<FireDanmaku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireDanmaku::getVideoId, videoId)
                .ge(FireDanmaku::getVideoTime, startTime)
                .le(FireDanmaku::getVideoTime, endTime)
                .orderByAsc(FireDanmaku::getVideoTime);

        List<FireDanmaku> danmakuList = this.list(wrapper);
        return danmakuList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DanmakuDTO convertToDTO(FireDanmaku danmaku) {
        DanmakuDTO dto = new DanmakuDTO();
        dto.setId(danmaku.getId());
        dto.setRoomId(danmaku.getRoomId());
        dto.setVideoId(danmaku.getVideoId());
        dto.setUserId(danmaku.getUserId());
        dto.setContent(danmaku.getContent());
        dto.setVideoTime(danmaku.getVideoTime());
        dto.setColor(danmaku.getColor());
        dto.setPosition(danmaku.getPosition());
        dto.setCreateTime(danmaku.getCreateTime());

        // 获取用户信息
        KhUser user = userService.getById(danmaku.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setAvatar(user.getAvatar());
        }

        return dto;
    }
}
