package com.kh.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.boot.entity.KhFriend;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友关系 Mapper
 *
 * @author harlan
 * @since 2024-01-20
 */
@Mapper
public interface FriendMapper extends BaseMapper<KhFriend> {
}
