package com.kh.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.boot.entity.KhPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<KhPermission> {

    /**
     * Get permissions by user ID
     */
    List<KhPermission> selectByUserId(@Param("userId") String userId);

    /**
     * Get menu list by user ID (filter out buttons)
     */
    List<KhPermission> selectMenuByUserId(@Param("userId") String userId);
}
