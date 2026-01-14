package com.kh.boot.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity Base Class
 */
@Data
public abstract class KhBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary Key ID")
    @TableId(type = IdType.ASSIGN_ID)
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

    @Version
    @Schema(description = "Optimistic Lock Version")
    private Integer version;

    @TableLogic
    @Schema(description = "Logic Delete (0: Normal, 1: Deleted)")
    private Integer delFlag;
}
