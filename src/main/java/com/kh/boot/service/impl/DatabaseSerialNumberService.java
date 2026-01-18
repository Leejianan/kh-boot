package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kh.boot.entity.KhSerialNumber;
import com.kh.boot.mapper.SerialNumberMapper;
import com.kh.boot.service.SerialNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(RedisSerialNumberService.class)
public class DatabaseSerialNumberService implements SerialNumberService {

    private final SerialNumberMapper serialNumberMapper;

    @Override
    @Transactional
    public synchronized String generateNext(String businessKey, String defaultPrefix, String defaultDateFormat,
            int defaultWidth) {
        KhSerialNumber sn = serialNumberMapper.selectOne(new QueryWrapper<KhSerialNumber>()
                .eq("business_key", businessKey));

        if (sn == null) {
            sn = new KhSerialNumber();
            sn.setId(UUID.randomUUID().toString().replace("-", ""));
            sn.setBusinessKey(businessKey);
            sn.setPrefix(defaultPrefix);
            sn.setCurrentValue(0L);
            sn.setUpdateTime(new Date());
            // Persistence of default rules if not exists
            sn.setRulePrefix(defaultPrefix);
            sn.setRuleDateFormat(defaultDateFormat);
            sn.setRuleWidth(defaultWidth);
            serialNumberMapper.insert(sn);
        }

        // Use database rule if exists, otherwise use default
        String prefix = sn.getRulePrefix() != null ? sn.getRulePrefix() : defaultPrefix;
        String dateFormat = sn.getRuleDateFormat() != null ? sn.getRuleDateFormat() : defaultDateFormat;
        int width = sn.getRuleWidth() != null ? sn.getRuleWidth() : defaultWidth;

        String currentDateStr = "";
        if (dateFormat != null && !dateFormat.isEmpty()) {
            currentDateStr = new SimpleDateFormat(dateFormat).format(new Date());
        }

        // Check if date has changed (if applicable)
        if (!currentDateStr.isEmpty() && !currentDateStr.equals(sn.getDatePart())) {
            sn.setCurrentValue(1L);
            sn.setDatePart(currentDateStr);
        } else {
            sn.setCurrentValue(sn.getCurrentValue() + 1);
        }

        sn.setUpdateTime(new Date());
        serialNumberMapper.updateById(sn);

        return format(prefix, currentDateStr, sn.getCurrentValue(), width);
    }

    @Override
    @Transactional
    public synchronized void reset(String businessKey, long nextValue) {
        KhSerialNumber sn = serialNumberMapper.selectOne(new QueryWrapper<KhSerialNumber>()
                .eq("business_key", businessKey));
        if (sn != null) {
            sn.setCurrentValue(nextValue - 1);
            sn.setUpdateTime(new Date());
            serialNumberMapper.updateById(sn);
        }
    }

    @Override
    public List<KhSerialNumber> list() {
        return serialNumberMapper.selectList(null);
    }

    @Override
    @Transactional
    public void saveRule(KhSerialNumber serialNumber) {
        // Check if rule already exists by ID or Business Key
        KhSerialNumber existing = null;
        if (serialNumber.getId() != null) {
            existing = serialNumberMapper.selectById(serialNumber.getId());
        } else {
            existing = serialNumberMapper.selectOne(new QueryWrapper<KhSerialNumber>()
                    .eq("business_key", serialNumber.getBusinessKey()));
        }

        if (existing == null) {
            if (serialNumber.getId() == null) {
                serialNumber.setId(UUID.randomUUID().toString().replace("-", ""));
            }
            try {
                serialNumberMapper.insert(serialNumber);
            } catch (Exception e) {
                // Concurrency or Logical Delete conflict: verify if it really exists now
                KhSerialNumber retrySelect = serialNumberMapper.selectOne(new QueryWrapper<KhSerialNumber>()
                        .eq("business_key", serialNumber.getBusinessKey()));
                if (retrySelect != null) {
                    // Found it after all (maybe race condition), perform update logic
                    retrySelect.setRulePrefix(serialNumber.getRulePrefix());
                    retrySelect.setRuleDateFormat(serialNumber.getRuleDateFormat());
                    retrySelect.setRuleWidth(serialNumber.getRuleWidth());
                    serialNumberMapper.updateById(retrySelect);
                } else {
                    // Still can't find it but insert failed? Likely a logical delete ghost record.
                    // Just log warn and move on, fixing manual DB intervention might be needed for
                    // logic delete rows.
                    log.warn("Failed to insert rule for [{}], possibly exists but marked as deleted: {}",
                            serialNumber.getBusinessKey(), e.getMessage());
                }
            }
        } else {
            // Update existing rule properties
            existing.setRulePrefix(serialNumber.getRulePrefix());
            existing.setRuleDateFormat(serialNumber.getRuleDateFormat());
            existing.setRuleWidth(serialNumber.getRuleWidth());
            // Make sure ID is set for update
            serialNumber.setId(existing.getId());
            serialNumberMapper.updateById(existing);
        }
    }

    private String format(String prefix, String datePart, Long value, int width) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null)
            sb.append(prefix);
        if (datePart != null)
            sb.append(datePart);
        sb.append(String.format("%0" + width + "d", value));
        return sb.toString();
    }
}
