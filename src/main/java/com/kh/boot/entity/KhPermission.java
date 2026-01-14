package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_permission")
@Schema(description = "Permission Entity")
public class KhPermission extends KhBaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Permission Name")
    private String name;

    @Schema(description = "Parent ID")
    private String parentId;

    @Schema(description = "Permission Key (e.g. system:user:add)")
    private String permissionKey;

    @Schema(description = "Resource Type (0:Directory, 1:Menu, 2:Button)")
    private Integer type;

    @Schema(description = "Router Path")
    private String path;

    @Schema(description = "Component Path")
    private String component;

    @Schema(description = "Icon")
    private String icon;

    @Schema(description = "Sort Order")
    private Integer sort;

    @Schema(description = "Status (1:Normal, 0:Disabled)")
    private Integer status;

    @Schema(description = "Children List")
    @TableField(exist = false)
    private List<KhPermission> children;
}
