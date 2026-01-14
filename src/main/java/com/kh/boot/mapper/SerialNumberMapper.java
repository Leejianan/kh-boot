package com.kh.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.boot.entity.KhSerialNumber;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SerialNumberMapper extends BaseMapper<KhSerialNumber> {
}
