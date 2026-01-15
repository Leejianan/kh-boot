package com.kh.boot.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * Paginated Data Wrapper
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Data
@Schema(description = "Paginated Data Wrapper")
public class PageData<T> implements Serializable {

    @Schema(description = "Total records", example = "100")
    private Long total;

    @Schema(description = "Current page", example = "1")
    private Long current;

    @Schema(description = "Page size", example = "10")
    private Long size;

    @Schema(description = "Data records")
    private List<T> records;

    public PageData() {
    }

    public PageData(Long total, Long current, Long size, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.records = records;
    }

    /**
     * Create PageData from MyBatis-Plus IPage
     */
    public static <T> PageData<T> build(IPage<T> page) {
        return new PageData<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }
}
