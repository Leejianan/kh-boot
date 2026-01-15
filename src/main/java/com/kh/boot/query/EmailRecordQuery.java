package com.kh.boot.query;

import com.kh.boot.query.base.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * Email Record Query
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailRecordQuery extends BasePageQuery {

    @Schema(description = "Recipient Email")
    private String sendTo;

    @Schema(description = "Sending Result: 1-Success, 0-Fail")
    private Integer sendResult;
}
