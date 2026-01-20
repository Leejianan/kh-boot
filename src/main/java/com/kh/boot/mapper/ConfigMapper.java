package com.kh.boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kh.boot.entity.KhConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigMapper extends BaseMapper<KhConfig> {
}
