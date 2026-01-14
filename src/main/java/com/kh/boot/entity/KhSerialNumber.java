package com.kh.boot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

@TableName("kh_serial_number")
public class KhSerialNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private String id;

    private String businessKey;

    private String prefix;

    private String datePart;

    private Long currentValue;

    private String rulePrefix;

    private String ruleDateFormat;

    private Integer ruleWidth;

    private Date updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDatePart() {
        return datePart;
    }

    public void setDatePart(String datePart) {
        this.datePart = datePart;
    }

    public Long getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
    }

    public String getRulePrefix() {
        return rulePrefix;
    }

    public void setRulePrefix(String rulePrefix) {
        this.rulePrefix = rulePrefix;
    }

    public String getRuleDateFormat() {
        return ruleDateFormat;
    }

    public void setRuleDateFormat(String ruleDateFormat) {
        this.ruleDateFormat = ruleDateFormat;
    }

    public Integer getRuleWidth() {
        return ruleWidth;
    }

    public void setRuleWidth(Integer ruleWidth) {
        this.ruleWidth = ruleWidth;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
