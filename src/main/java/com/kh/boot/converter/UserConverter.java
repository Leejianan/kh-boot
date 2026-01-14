package com.kh.boot.converter;

import com.kh.boot.dto.KhUserDTO;
import com.kh.boot.dto.KhUserRegisterDTO;
import com.kh.boot.entity.KhUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserConverter {

    KhUserDTO toDto(KhUser user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "auditTime", ignore = true)
    @Mapping(target = "auditor", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createByName", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateByName", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    KhUser toEntity(KhUserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userCode", ignore = true)
    @Mapping(target = "auditTime", ignore = true)
    @Mapping(target = "auditor", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createByName", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateByName", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "auditStatus", ignore = true)
    KhUser toEntity(KhUserRegisterDTO dto);

    List<KhUserDTO> toDtoList(List<KhUser> users);
}
