package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_role")
@Schema(description = "Role Entity")
public class KhRole extends KhBaseEntity {

    @Schema(description = "Role Name")
    private String name;

    @Schema(description = "Role Key (e.g. admin)")
    private String roleKey;

    @Schema(description = "Sort Order")
    private Integer sort;

    @Schema(description = "Status (1:Normal, 0:Disabled)")
    private Integer status;
}
