package com.kh.boot.query;

import com.kh.boot.query.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * User Query
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends BasePageQuery {

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Phone Number")
    private String phone;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Status: 1-Normal, 0-Disabled")
    private Integer status;
}
