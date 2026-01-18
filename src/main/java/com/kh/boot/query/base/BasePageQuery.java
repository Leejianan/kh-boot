package com.kh.boot.query.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

/**
 * <p>
 * Base Pagination Query
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Data
public class BasePageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Page Number (Current)", example = "1", defaultValue = "1")
    private Integer current = 1;

    @Schema(description = "Page Size", example = "10", defaultValue = "10")
    private Integer size = 10;

    @Schema(description = "Order By Column")
    private String orderBy;

    @Schema(description = "Is Ascending", defaultValue = "true")
    private Boolean isAsc = true;

    /**
     * Convert to MyBatis-Plus Page object
     */
    public <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> toPage() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> pageParam = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                current, size);
        if (org.springframework.util.StringUtils.hasText(orderBy)) {
            com.baomidou.mybatisplus.core.metadata.OrderItem item = isAsc
                    ? OrderItem.asc(StringUtils.camelToUnderline(orderBy))
                    : OrderItem.desc(StringUtils.camelToUnderline(orderBy));
            pageParam.addOrder(item);
        }
        return pageParam;
    }
}
