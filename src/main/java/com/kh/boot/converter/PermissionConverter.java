package com.kh.boot.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionConverter {

    @Mapping(target = "label", source = "name")
    @Mapping(target = "perms", source = "permissionKey")
    com.kh.boot.dto.KhPermissionDTO toDto(com.kh.boot.entity.KhPermission permission);

    @Mapping(target = "name", source = "label")
    @Mapping(target = "permissionKey", source = "perms")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createByName", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateByName", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    com.kh.boot.entity.KhPermission toEntity(com.kh.boot.dto.KhPermissionDTO dto);

    List<com.kh.boot.dto.KhPermissionDTO> toDtoList(List<com.kh.boot.entity.KhPermission> permissions);
}
