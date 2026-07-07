package com.mojian.service;

import com.mojian.sensitive.domain.SysSensitiveWord;
import com.mojian.sensitive.domain.SysSensitiveWordWhitelist;

import java.util.List;

/**
 * 敏感词 Service 接口
 */
public interface ISysSensitiveWordService {

    /**
     * 查询敏感词列表
     */
    List<SysSensitiveWord> selectList();

    /**
     * 新增敏感词
     */
    int insert(SysSensitiveWord word);

    /**
     * 批量新增敏感词
     */
    int batchInsert(List<SysSensitiveWord> words);

    /**
     * 删除敏感词
     */
    int deleteById(Long id);

    /**
     * 批量删除敏感词
     */
    int deleteByIds(List<Long> ids);

    /**
     * 查询白名单列表
     */
    List<SysSensitiveWordWhitelist> selectWhitelist();

    /**
     * 新增白名单
     */
    int insertWhitelist(SysSensitiveWordWhitelist whitelist);

    /**
     * 批量新增白名单
     */
    int batchInsertWhitelist(List<SysSensitiveWordWhitelist> whitelists);

    /**
     * 删除白名单
     */
    int deleteWhitelistById(Long id);

    /**
     * 重新加载敏感词库
     */
    void reload();
}
