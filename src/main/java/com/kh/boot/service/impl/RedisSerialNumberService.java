package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kh.boot.entity.KhSerialNumber;
import com.kh.boot.mapper.SerialNumberMapper;
import com.kh.boot.service.SerialNumberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisSerialNumberService implements SerialNumberService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SerialNumberMapper serialNumberMapper;

    @Override
    public String generateNext(String businessKey, String defaultPrefix, String defaultDateFormat, int defaultWidth) {
        // Find rule in DB first
        KhSerialNumber sn = serialNumberMapper.selectOne(
                new QueryWrapper<KhSerialNumber>()
                        .eq("business_key", businessKey));

        String prefix = defaultPrefix;
        String dateFormat = defaultDateFormat;
        int width = defaultWidth;

        if (sn != null) {
            if (sn.getRulePrefix() != null)
                prefix = sn.getRulePrefix();
            if (sn.getRuleDateFormat() != null)
                dateFormat = sn.getRuleDateFormat();
            if (sn.getRuleWidth() != null)
                width = sn.getRuleWidth();
        }

        String currentDateStr = "";
        if (dateFormat != null && !dateFormat.isEmpty()) {
            currentDateStr = new SimpleDateFormat(dateFormat).format(new Date());
        }

        // Redis Key: SN:BUSINESS_KEY:DATE (if date exists)
        String redisKey = "sn:" + businessKey + (currentDateStr.isEmpty() ? "" : ":" + currentDateStr);

        Long nextValue = redisTemplate.opsForValue().increment(redisKey);

        // Set expiry if date is present (e.g. 2 days) to clean up old keys
        if (nextValue != null && nextValue == 1 && !currentDateStr.isEmpty()) {
            redisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
        }

        return format(prefix, currentDateStr, nextValue, width);
    }

    @Override
    public void reset(String businessKey, long nextValue) {
        String redisKey = "sn:" + businessKey;
        redisTemplate.opsForValue().set(redisKey, nextValue - 1);

        // Also update DB if exists to keep sync
        KhSerialNumber sn = serialNumberMapper.selectOne(
                new QueryWrapper<KhSerialNumber>()
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
                // Concurrency conflict: verify if it really exists now
                KhSerialNumber retrySelect = serialNumberMapper.selectOne(new QueryWrapper<KhSerialNumber>()
                        .eq("business_key", serialNumber.getBusinessKey()));
                if (retrySelect != null) {
                    // Found it after all (race condition), perform update
                    retrySelect.setRulePrefix(serialNumber.getRulePrefix());
                    retrySelect.setRuleDateFormat(serialNumber.getRuleDateFormat());
                    retrySelect.setRuleWidth(serialNumber.getRuleWidth());
                    serialNumberMapper.updateById(retrySelect);
                } else {
                    log.warn("Failed to insert rule for [{}]: {}", serialNumber.getBusinessKey(), e.getMessage());
                }
            }
        } else {
            // Update existing rule properties
            existing.setRulePrefix(serialNumber.getRulePrefix());
            existing.setRuleDateFormat(serialNumber.getRuleDateFormat());
            existing.setRuleWidth(serialNumber.getRuleWidth());
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
        sb.append(String.format("%0" + width + "d", value != null ? value : 0));
        return sb.toString();
    }
}
