package com.kh.boot.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Route View Object for frontend menu rendering
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KhRouterVo {
    /**
     * Route name (must be unique)
     */
    private String name;

    /**
     * Route path
     */
    private String path;

    /**
     * Component path (e.g. Layout, system/user/index)
     */
    private String component;

    /**
     * Meta info
     */
    private KhMetaVo meta;

    /**
     * Children routes
     */
    private List<KhRouterVo> children;
}
