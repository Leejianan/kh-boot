package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_user")
@Schema(description = "User Entity")
public class KhUser extends KhBaseEntity {

    @Schema(description = "Business User Code")
    @com.kh.boot.annotation.BusinessCode(prefix = "U", dateFormat = "yyyyMMdd", width = 4)
    private String userCode;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Password", example = "123456")
    private String password;

    @Schema(description = "Phone Number", example = "13800000000")
    private String phone;

    @Schema(description = "Email", example = "john@example.com")
    private String email;

    @Schema(description = "Status: 1-Normal, 0-Disabled", example = "1")
    private Integer status;

    @Schema(description = "Audit Status: 0-Pending, 1-Approved, 2-Rejected", example = "1")
    private Integer auditStatus;

    private Date auditTime;
    private String auditor;
}
