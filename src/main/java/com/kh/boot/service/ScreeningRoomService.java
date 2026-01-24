package com.kh.boot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.dto.ScreeningRoomDTO;
import com.kh.boot.entity.FireScreeningRoom;

import java.util.List;

/**
 * 放映室服务接口
 *
 * @author harlan
 * @since 2026-01-23
 */
public interface ScreeningRoomService extends IService<FireScreeningRoom> {

    /**
     * 分页查询放映室列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @param name    房间名称（模糊查询）
     * @return 放映室分页列表
     */
    IPage<ScreeningRoomDTO> getRoomList(int current, int size, String name);

    /**
     * 创建放映室
     *
     * @param name     房间名称
     * @param password 密码（可选）
     * @return 新建的放映室
     */
    FireScreeningRoom createRoom(String name, String password);

    /**
     * 加入放映室
     *
     * @param roomId   放映室ID
     * @param password 密码（如有）
     */
    void joinRoom(String roomId, String password);

    /**
     * 离开放映室
     *
     * @param roomId 放映室ID
     */
    void leaveRoom(String roomId);

    /**
     * 切换视频
     *
     * @param roomId  放映室ID
     * @param videoId 视频ID
     */
    void switchVideo(String roomId, String videoId);

    /**
     * 切换视频（内部使用，不验证权限）
     * 供 WebSocket 调用
     *
     * @param roomId  放映室ID
     * @param videoId 视频ID
     */
    void switchVideoInternal(String roomId, String videoId);

    /**
     * 同步播放状态
     *
     * @param roomId      放映室ID
     * @param isPlaying   是否播放中
     * @param currentTime 当前时间
     */
    void syncPlayStatus(String roomId, boolean isPlaying, int currentTime);

    /**
     * 获取放映室详情（含成员列表）
     *
     * @param roomId 放映室ID
     * @return 放映室详情
     */
    ScreeningRoomDTO getRoomDetail(String roomId);

    /**
     * 获取房间内成员用户名列表
     *
     * @param roomId 放映室ID
     * @return 成员用户名列表
     */
    List<String> getRoomMemberUsernames(String roomId);

    /**
     * 关闭放映室（房主操作）
     *
     * @param roomId 放映室ID
     */
    void closeRoom(String roomId);
}
