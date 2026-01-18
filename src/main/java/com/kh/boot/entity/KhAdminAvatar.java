package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * System Default Avatars
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_admin_avatar")
@Schema(name = "KhAdminAvatar", description = "System Default Avatars")
public class KhAdminAvatar extends KhBaseEntity {

    @Schema(description = "Avatar Name")
    private String name;

    @Schema(description = "Avatar Path")
    private String path;

    @Schema(description = "Avatar Type: DEFAULT, NFT")
    private String type;
}
