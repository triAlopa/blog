package com.mojian.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mojian.sensitive.domain.SysSensitiveWordWhitelist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 敏感词白名单 Mapper
 */
@Mapper
public interface SysSensitiveWordWhitelistMapper extends BaseMapper<SysSensitiveWordWhitelist> {

}
