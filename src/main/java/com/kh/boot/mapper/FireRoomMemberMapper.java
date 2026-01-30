package com.kh.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.boot.entity.FireRoomMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 放映室成员 Mapper
 *
 * @author harlan
 * @since 2026-01-23
 */
@Mapper
public interface FireRoomMemberMapper extends BaseMapper<FireRoomMember> {

    @org.apache.ibatis.annotations.Select("SELECT count(*) FROM fire_room_member WHERE room_id = #{roomId} AND user_id = #{userId}")
    int countIncludingDeleted(@org.apache.ibatis.annotations.Param("roomId") String roomId,
            @org.apache.ibatis.annotations.Param("userId") String userId);
}
