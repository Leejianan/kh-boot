package com.kh.boot.query;

import com.kh.boot.query.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Role Query Object
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Role Query Object")
public class RoleQuery extends BasePageQuery {

    @Schema(description = "Role Name")
    private String name;
}
