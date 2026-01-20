package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.entity.KhConfig;
import com.kh.boot.mapper.ConfigMapper;
import com.kh.boot.service.ConfigService;
import com.kh.boot.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, KhConfig> implements ConfigService {

    // Simple in-memory cache for configs
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Override
    public void refreshCache() {
        configCache.clear();
        List<KhConfig> configs = list();
        for (KhConfig config : configs) {
            if (config.getConfigKey() != null && config.getConfigValue() != null) {
                configCache.put(config.getConfigKey(), config.getConfigValue());
            }
        }
    }

    @Override
    public String getValueByKey(String key) {
        String value = configCache.get(key);
        if (value == null) {
            // Fallback to DB if cache miss (e.g. newly added)
            KhConfig config = getOne(new LambdaQueryWrapper<KhConfig>().eq(KhConfig::getConfigKey, key));
            if (config != null) {
                value = config.getConfigValue();
                configCache.put(key, value);
            }
        }
        return value;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createConfig(KhConfig config) {
        // Check uniqueness of key
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<KhConfig>()
                .eq(KhConfig::getConfigKey, config.getConfigKey()));
        if (count > 0) {
            throw new RuntimeException("Parameter key '" + config.getConfigKey() + "' already exists");
        }

        EntityUtils.initInsert(config);
        boolean result = save(config);
        if (result) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(KhConfig config) {
        KhConfig existing = getById(config.getId());
        if (existing == null) {
            throw new RuntimeException("Config not found");
        }

        // Cannot change key for built-in configs usually, but let's allow flexibility
        // with care
        if (!existing.getConfigKey().equals(config.getConfigKey())) {
            Long count = baseMapper.selectCount(new LambdaQueryWrapper<KhConfig>()
                    .eq(KhConfig::getConfigKey, config.getConfigKey())
                    .ne(KhConfig::getId, config.getId()));
            if (count > 0) {
                throw new RuntimeException("Parameter key '" + config.getConfigKey() + "' already exists");
            }
            // Remove old key from cache
            configCache.remove(existing.getConfigKey());
        }

        EntityUtils.initUpdate(config);
        boolean result = updateById(config);

        if (result) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(String id) {
        KhConfig config = getById(id);
        if (config == null)
            return true;

        if ("Y".equals(config.getConfigType())) {
            throw new RuntimeException("Cannot delete system built-in config");
        }

        boolean result = removeById(id);
        if (result) {
            configCache.remove(config.getConfigKey());
        }
        return result;
    }
}
