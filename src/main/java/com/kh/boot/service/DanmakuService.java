package com.kh.boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.dto.DanmakuDTO;
import com.kh.boot.entity.FireDanmaku;

import java.util.List;

/**
 * 弹幕服务接口
 *
 * @author harlan
 * @since 2026-01-23
 */
public interface DanmakuService extends IService<FireDanmaku> {

    /**
     * 发送弹幕
     *
     * @param userId    用户ID
     * @param username  用户名
     * @param roomId    放映室ID
     * @param videoId   视频ID
     * @param content   内容
     * @param videoTime 视频时间点
     * @param color     颜色
     * @param position  位置
     * @return 弹幕信息
     */
    DanmakuDTO sendDanmaku(String userId, String username, String roomId, String videoId, String content, int videoTime,
            String color, String position);

    /**
     * 获取视频弹幕列表
     *
     * @param videoId 视频ID
     * @return 弹幕列表
     */
    List<DanmakuDTO> getDanmakuList(String videoId);

    /**
     * 获取特定时间段的弹幕
     *
     * @param videoId   视频ID
     * @param startTime 开始时间（秒）
     * @param endTime   结束时间（秒）
     * @return 弹幕列表
     */
    List<DanmakuDTO> getDanmakuByTimeRange(String videoId, int startTime, int endTime);
}
