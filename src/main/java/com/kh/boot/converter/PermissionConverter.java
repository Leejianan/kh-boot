package com.kh.boot.converter;

import com.kh.boot.dto.KhPermissionDTO;
import com.kh.boot.entity.KhPermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper
public interface PermissionConverter {

    PermissionConverter INSTANCE = Mappers.getMapper(PermissionConverter.class);

    @Mapping(target = "label", source = "name")
    @Mapping(target = "perms", source = "permissionKey")
    KhPermissionDTO toDto(KhPermission permission);

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
    KhPermission toEntity(KhPermissionDTO dto);

    List<KhPermissionDTO> toDtoList(List<KhPermission> permissions);
}
