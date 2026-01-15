package com.kh.boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kh.boot.converter.EmailRecordConverter;
import com.kh.boot.dto.KhEmailRecordDTO;
import com.kh.boot.entity.KhEmailRecord;
import com.kh.boot.mapper.EmailRecordMapper;
import com.kh.boot.query.EmailRecordQuery;
import com.kh.boot.service.EmailRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Email Sending Record Service Implementation
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Slf4j
@Service
public class EmailRecordServiceImpl extends ServiceImpl<EmailRecordMapper, KhEmailRecord>
        implements EmailRecordService {

    @Override
    public IPage<KhEmailRecordDTO> page(EmailRecordQuery query) {
        Page<KhEmailRecord> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<KhEmailRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getSendTo()), KhEmailRecord::getSendTo, query.getSendTo());
        wrapper.eq(query.getSendResult() != null, KhEmailRecord::getSendResult, query.getSendResult());
        wrapper.orderByDesc(KhEmailRecord::getCreateTime);
        IPage<KhEmailRecord> pageResult = this.page(pageParam, wrapper);
        return pageResult.convert(EmailRecordConverter.INSTANCE::toDto);
    }

    @Async
    @Override
    public void saveRecord(String to, String subject, String content, boolean result, String failReason) {
        try {
            KhEmailRecord record = new KhEmailRecord();
            record.setSendTo(to);
            record.setSendSubject(subject);
            record.setSendContent(content);
            record.setSendResult(result ? 1 : 0);
            record.setFailReason(failReason);
            this.save(record);
        } catch (Exception e) {
            log.error("Failed to save email record: {}", e.getMessage());
        }
    }
}
