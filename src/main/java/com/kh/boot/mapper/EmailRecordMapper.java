package com.kh.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.boot.entity.KhEmailRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Email Sending Record Mapper Interface
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Mapper
public interface EmailRecordMapper extends BaseMapper<KhEmailRecord> {

}
