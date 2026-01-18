package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.kh.boot.entity.base.KhBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kh_serial_number")
public class KhSerialNumber extends KhBaseEntity {

    private static final long serialVersionUID = 1L;

    private String businessKey;

    private String prefix;

    private String datePart;

    private Long currentValue;

    private String rulePrefix;

    private String ruleDateFormat;

    private Integer ruleWidth;
}
