package com.kh.boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.entity.KhConfig;

public interface ConfigService extends IService<KhConfig> {

    /**
     * Get config value by key (with local caching support)
     * e.g. "sys.mail.host"
     */
    String getValueByKey(String key);

    /**
     * Refresh config cache (can be used after DB updates)
     */
    void refreshCache();

    /**
     * Create new config
     */
    boolean createConfig(KhConfig config);

    /**
     * Update existing config
     */
    boolean updateConfig(KhConfig config);

    /**
     * Delete config
     */
    boolean deleteConfig(String id);
}
