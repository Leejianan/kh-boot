package com.kh.boot.dto;

import com.kh.boot.dto.base.KhBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Permission DTO for Management
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Permission DTO for Management")
public class KhPermissionDTO extends KhBaseDTO {

    @Schema(description = "Parent ID")
    private String parentId;

    @Schema(description = "Display Name / Label")
    private String label;

    @Schema(description = "Resource Type (0:Directory, 1:Menu, 2:Button)")
    private Integer type;

    @Schema(description = "Router Path")
    private String path;

    @Schema(description = "Component Path")
    private String component;

    @Schema(description = "Permission Key / perms")
    private String perms;

    @Schema(description = "Icon")
    private String icon;

    @Schema(description = "Sort Order")
    private Integer sort;

    @Schema(description = "Status")
    private String status;
}
