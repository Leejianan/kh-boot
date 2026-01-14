package com.kh.boot.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO Base Class
 */
@Data
public abstract class KhBaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary Key ID")
    private String id;

    @Schema(description = "Create Time")
    private Date createTime;

    @Schema(description = "Update Time")
    private Date updateTime;

    @Schema(description = "Creator ID")
    private String createBy;

    @Schema(description = "Creator Name")
    private String createByName;

    @Schema(description = "Updater ID")
    private String updateBy;

    @Schema(description = "Updater Name")
    private String updateByName;
}
