package com.kh.boot.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserConverter {

    com.kh.boot.dto.KhUserDTO toDto(com.kh.boot.entity.KhUser user);

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
    com.kh.boot.entity.KhUser toEntity(com.kh.boot.dto.KhUserDTO dto);

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
    com.kh.boot.entity.KhUser toEntity(com.kh.boot.dto.KhUserRegisterDTO dto);

    List<com.kh.boot.dto.KhUserDTO> toDtoList(List<com.kh.boot.entity.KhUser> users);
}
