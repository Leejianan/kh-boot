package com.kh.boot.dto;

import com.kh.boot.dto.base.KhBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "User DTO")
public class KhUserDTO extends KhBaseDTO {

    @Schema(description = "Business User Code")
    private String userCode;

    private String username;
    private String phone;
    private String email;
    private String realName;
    private Integer gender;
    private String avatar;
    private Integer status;
    private Integer auditStatus;

    @Schema(description = "User's assigned role names")
    private List<String> roleNames;
}
