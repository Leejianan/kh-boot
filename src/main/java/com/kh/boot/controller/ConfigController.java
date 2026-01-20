package com.kh.boot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kh.boot.common.PageData;
import com.kh.boot.common.Result;
import com.kh.boot.controller.base.BaseController;
import com.kh.boot.entity.KhConfig;
import com.kh.boot.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

@Tag(name = "System Configuration")
@RestController
@RequestMapping("/admin/system/config")
public class ConfigController extends BaseController {

    @Autowired
    private ConfigService configService;

    @Operation(summary = "Get Config List")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<PageData<KhConfig>> page(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String configName,
            @RequestParam(required = false) String configKey,
            @RequestParam(required = false) String configType) {
        Page<KhConfig> pageParam = new Page<>(current, size);
        LambdaQueryWrapper<KhConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(configName), KhConfig::getConfigName, configName);
        wrapper.like(StringUtils.hasText(configKey), KhConfig::getConfigKey, configKey);
        wrapper.eq(StringUtils.hasText(configType), KhConfig::getConfigType, configType);
        wrapper.orderByDesc(KhConfig::getCreateTime);

        IPage<KhConfig> result = configService.page(pageParam, wrapper);
        return success(PageData.build(result));
    }

    @Operation(summary = "Get Config by Key")
    @GetMapping(value = "/configKey/{configKey}")
    public Result<String> getConfigKey(@PathVariable String configKey) {
        return success(configService.getValueByKey(configKey));
    }

    @Operation(summary = "Create Config")
    @PostMapping
    @PreAuthorize("hasAuthority('system:config:add')")
    public Result<Boolean> add(@RequestBody KhConfig config) {
        return success(configService.createConfig(config));
    }

    @Operation(summary = "Update Config")
    @PutMapping
    @PreAuthorize("hasAuthority('system:config:edit')")
    public Result<Boolean> update(@RequestBody KhConfig config) {
        return success(configService.updateConfig(config));
    }

    @Operation(summary = "Delete Config")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:delete')")
    public Result<Boolean> delete(@PathVariable String id) {
        return success(configService.deleteConfig(id));
    }

    @Operation(summary = "Refresh Cache")
    @PutMapping("/refreshCache")
    @PreAuthorize("hasAuthority('system:config:remove')")
    public Result<Boolean> refreshCache() {
        configService.refreshCache();
        return success(true);
    }
}
