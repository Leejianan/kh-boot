package com.kh.boot.converter;

import com.kh.boot.dto.KhRoleDTO;
import com.kh.boot.entity.KhRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    @Mapping(target = "permissionIds", ignore = true)
    KhRoleDTO toDto(KhRole role);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createByName", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateByName", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    KhRole toEntity(KhRoleDTO roleDTO);

    List<KhRoleDTO> toDtoList(List<KhRole> roles);
}
