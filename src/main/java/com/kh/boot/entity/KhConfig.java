package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_config")
public class KhConfig extends KhBaseEntity {

    /**
     * Parameter Name
     */
    private String configName;

    /**
     * Parameter Key
     */
    private String configKey;

    /**
     * Parameter Value
     */
    private String configValue;

    /**
     * System Built-in (Y=Yes, N=No)
     */
    private String configType;

    /**
     * Remark
     */
    private String remark;
}
