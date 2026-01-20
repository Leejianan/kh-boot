package com.kh.boot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh.boot.dto.KhEmailRecordDTO;
import com.kh.boot.entity.KhEmailRecord;
import com.kh.boot.query.EmailRecordQuery;

import java.util.List;

/**
 * <p>
 * Email Sending Record Service Interface
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
public interface EmailRecordService extends IService<KhEmailRecord> {

    /**
     * Get email record page
     *
     * @param query query parameters
     * @return paginated records
     */
    IPage<KhEmailRecordDTO> page(EmailRecordQuery query);

    /**
     * Async save email record
     *
     * @param to         recipient
     * @param subject    subject
     * @param content    content
     * @param result     result (true: success, false: fail)
     * @param failReason failure reason
     */
    void saveRecord(String to, String subject, String content, boolean result, String failReason);

    /**
     * Delete email records by IDs
     *
     * @param ids record IDs
     */
    void deleteEmailRecords(List<Long> ids);
}
