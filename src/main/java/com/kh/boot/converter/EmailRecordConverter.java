package com.kh.boot.converter;

import com.kh.boot.dto.KhEmailRecordDTO;
import com.kh.boot.entity.KhEmailRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * <p>
 * Email Record Converter
 * </p>
 *
 * @author harlan
 * @since 2024-01-15
 */
@Mapper
public interface EmailRecordConverter {

    EmailRecordConverter INSTANCE = Mappers.getMapper(EmailRecordConverter.class);

    @Mapping(source = "sendSubject", target = "subject")
    @Mapping(source = "sendContent", target = "content")
    KhEmailRecordDTO toDto(KhEmailRecord entity);

    @Mapping(source = "subject", target = "sendSubject")
    @Mapping(source = "content", target = "sendContent")
    KhEmailRecord toEntity(KhEmailRecordDTO dto);

    List<KhEmailRecordDTO> toDtoList(List<KhEmailRecord> list);
}
