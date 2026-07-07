package com.mojian.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mojian.sensitive.domain.SysSensitiveWord;
import com.mojian.sensitive.domain.SysSensitiveWordWhitelist;
import com.mojian.sensitive.listen.SensitiveWordChangeEvent;
import com.mojian.mapper.SysSensitiveWordMapper;
import com.mojian.mapper.SysSensitiveWordWhitelistMapper;
import com.mojian.sensitive.service.SensitiveWordBsHolder;
import com.mojian.service.ISysSensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 敏感词 Service 实现
 */
@Service
@RequiredArgsConstructor
public class SysSensitiveWordServiceImpl implements ISysSensitiveWordService {

    private final SysSensitiveWordMapper wordMapper;
    private final SysSensitiveWordWhitelistMapper whitelistMapper;
    private final SensitiveWordBsHolder sensitiveWordBsHolder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<SysSensitiveWord> selectList() {
        return wordMapper.selectList(
            Wrappers.<SysSensitiveWord>lambdaQuery()
                .eq(SysSensitiveWord::getStatus, "ACTIVE")
                .orderByDesc(SysSensitiveWord::getId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(SysSensitiveWord word) {
        int rows = wordMapper.insert(word);
        if (rows > 0) {
            // 发布事件，触发词库重新加载
            eventPublisher.publishEvent(new SensitiveWordChangeEvent(this, "ADD"));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysSensitiveWord> words) {
        int rows = 0;
        for (SysSensitiveWord word : words) {
            rows += wordMapper.insert(word);
        }
        if (rows > 0) {
            eventPublisher.publishEvent(new SensitiveWordChangeEvent(this, "BATCH_ADD"));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        int rows = wordMapper.deleteById(id);
        if (rows > 0) {
            eventPublisher.publishEvent(new SensitiveWordChangeEvent(this, "DELETE"));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids) {
        int rows = wordMapper.deleteBatchIds(ids);
        if (rows > 0) {
            eventPublisher.publishEvent(new SensitiveWordChangeEvent(this, "BATCH_DELETE"));
        }
        return rows;
    }

    @Override
    public List<SysSensitiveWordWhitelist> selectWhitelist() {
        return whitelistMapper.selectList(
            Wrappers.<SysSensitiveWordWhitelist>lambdaQuery()
                .orderByDesc(SysSensitiveWordWhitelist::getId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWhitelist(SysSensitiveWordWhitelist whitelist) {
        int rows = whitelistMapper.insert(whitelist);
        if (rows > 0) {
            eventPublisher.publishEvent(new SensitiveWordChangeEvent(this, "WHITELIST_ADD"));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertWhitelist(List<SysSensitiveWordWhitelist> whitelists) {
        int rows = 0;
        for (SysSensitiveWordWhitelist whitelist : whitelists) {
            rows += whitelistMapper.insert(whitelist);
        }
        if (rows > 0) {
            eventPublisher.publishEvent(new SensitiveWordChangeEvent(this, "WHITELIST_BATCH_ADD"));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWhitelistById(Long id) {
        int rows = whitelistMapper.deleteById(id);
        if (rows > 0) {
            eventPublisher.publishEvent(new SensitiveWordChangeEvent(this, "WHITELIST_DELETE"));
        }
        return rows;
    }

    @Override
    public void reload() {
        sensitiveWordBsHolder.reload();
    }
}
