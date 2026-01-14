package com.kh.boot.dto;

import com.kh.boot.dto.base.KhBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Role DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Role DTO")
public class KhRoleDTO extends KhBaseDTO {

    @Schema(description = "Role Name")
    private String name;

    @Schema(description = "Role Key")
    private String roleKey;

    @Schema(description = "Sort Order")
    private Integer sort;

    @Schema(description = "Status (1:Normal, 0:Disabled)")
    private Integer status;

    @Schema(description = "Permission IDs (for assignment)")
    private List<String> permissionIds;
}
