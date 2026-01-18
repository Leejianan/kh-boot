package com.kh.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.boot.entity.KhRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<KhRole> {

    /**
     * Get role names by user ID
     */
    @Select("SELECT DISTINCT r.name FROM kh_role r " +
            "INNER JOIN kh_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.del_flag = 0")
    List<String> selectRoleNamesByUserId(@Param("userId") String userId);
}
