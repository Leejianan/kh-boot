package com.kh.boot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Route meta information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KhMetaVo {
    /**
     * The title of the route
     */
    private String title;

    /**
     * The icon of the route
     */
    private String icon;
}
