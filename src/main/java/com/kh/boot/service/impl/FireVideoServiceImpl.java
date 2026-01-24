package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.entity.FireVideo;
import com.kh.boot.mapper.FireVideoMapper;
import com.kh.boot.service.FireVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 视频服务实现
 *
 * @author harlan
 * @since 2026-01-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FireVideoServiceImpl extends ServiceImpl<FireVideoMapper, FireVideo>
        implements FireVideoService {

    @Override
    public IPage<FireVideo> getVideoList(int current, int size, String title, String category) {
        LambdaQueryWrapper<FireVideo> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(title)) {
            wrapper.like(FireVideo::getTitle, title);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(FireVideo::getCategory, category);
        }
        wrapper.eq(FireVideo::getStatus, 1)
                .orderByDesc(FireVideo::getCreateTime);

        return this.page(new Page<>(current, size), wrapper);
    }
}
