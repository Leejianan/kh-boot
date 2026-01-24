package com.kh.boot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.entity.FireVideo;

/**
 * 视频服务接口
 *
 * @author harlan
 * @since 2026-01-23
 */
public interface FireVideoService extends IService<FireVideo> {

    /**
     * 分页查询视频列表
     *
     * @param current  当前页
     * @param size     每页大小
     * @param title    标题（模糊查询）
     * @param category 分类
     * @return 视频分页列表
     */
    IPage<FireVideo> getVideoList(int current, int size, String title, String category);
}
