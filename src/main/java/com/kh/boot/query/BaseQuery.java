package com.kh.boot.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Base Query Object")
public class BaseQuery implements Serializable {

    @Schema(description = "Current Page", example = "1")
    private Integer current = 1;

    @Schema(description = "Page Size", example = "10")
    private Integer size = 10;
}
