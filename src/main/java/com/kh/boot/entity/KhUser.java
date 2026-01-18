package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.constant.UserType;
import com.kh.boot.security.domain.BaseAuthentication;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.kh.boot.entity.base.KhBaseEntity;

/**
 * <p>
 * User Table
 * </p>
 *
 * @author harlan
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_user")
@Schema(name = "KhUser", description = "User Table")
public class KhUser extends KhBaseEntity implements BaseAuthentication {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String getUserType() {
        return UserType.ADMIN.getValue();
    }

    @Override
    public Integer getStatus() {
        return this.status;
    }

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

    @Schema(description = "Real Name", example = "John Doe")
    private String realName;

    @Schema(description = "Gender: 0-Unknown, 1-Male, 2-Female", example = "1")
    private Integer gender;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Status: 1-Normal, 0-Disabled", example = "1")
    private Integer status;

    @Schema(description = "Audit Status: 0-Pending, 1-Approved, 2-Rejected", example = "1")
    private Integer auditStatus;

    private Date auditTime;
    private String auditor;
}
