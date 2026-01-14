package com.kh.boot.util;

import com.kh.boot.entity.base.KhBaseEntity;
import java.util.Date;

/**
 * Entity Utility Class
 */
public class EntityUtils {

    private static SerialNumberGenerator serialNumberGenerator;

    public static void setSerialNumberGenerator(SerialNumberGenerator generator) {
        serialNumberGenerator = generator;
    }

    /**
     * Initialize common fields before insertion
     *
     * @param entity The entity to initialize
     */
    public static void initInsert(KhBaseEntity entity) {
        String userId = SecurityUtils.getUserId();
        String userName = SecurityUtils.getRealName();
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(userId);
        entity.setCreateByName(userName);
        entity.setUpdateBy(userId);
        entity.setUpdateByName(userName);
        entity.setVersion(1);
        entity.setDelFlag(0);

        if (serialNumberGenerator != null) {
            serialNumberGenerator.populateBusinessCode(entity);
        }
    }

    /**
     * Initialize common fields before update
     *
     * @param entity The entity to update
     */
    public static void initUpdate(KhBaseEntity entity) {
        entity.setUpdateTime(new Date());
        entity.setUpdateBy(SecurityUtils.getUserId());
        entity.setUpdateByName(SecurityUtils.getRealName());
    }
}
