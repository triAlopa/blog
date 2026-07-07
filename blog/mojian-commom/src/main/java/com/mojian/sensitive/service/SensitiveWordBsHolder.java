package com.mojian.sensitive.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mojian.sensitive.domain.SysSensitiveWord;
import com.mojian.sensitive.domain.SysSensitiveWordWhitelist;
import com.mojian.mapper.SysSensitiveWordMapper;
import com.mojian.mapper.SysSensitiveWordWhitelistMapper;
import com.github.houbb.sensitive.word.api.IWordReplace;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.check.WordChecks;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.github.houbb.sensitive.word.support.replace.WordReplaces;
import com.github.houbb.sensitive.word.support.resultcondition.WordResultConditions;
import com.github.houbb.sensitive.word.support.tag.WordTags;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词库持有者
 * <p>
 * 职责:
 * - 管理 SensitiveWordBs 实例的生命周期;
 * - 支持定时刷新和手动刷新;
 * - 提供敏感词校验方法.
 * <p>
 * 设计说明:
 * - 使用 volatile 保证多线程可见性;
 * - 定时任务兜底，防止事件丢失;
 * - 事件驱动保证实时性.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordBsHolder {

    private final SysSensitiveWordMapper wordMapper;
    private final SysSensitiveWordWhitelistMapper whitelistMapper;

    /**
     * 敏感词库实例（volatile 保证可见性）.
     */
    private volatile SensitiveWordBs wordBs;

    /**
     * 启动时加载词库.
     */
    @PostConstruct
    public void init() {
        log.info("敏感词库初始化开始...");
        reload();
        log.info("敏感词库初始化完成");
    }

    /**
     * 定时刷新词库（兜底，5分钟）.
     * <p>
     * 说明: 防止事件丢失导致词库不一致.
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledReload() {
        log.info("定时刷新敏感词库...");
        reload();
    }

    /**
     * 手动刷新词库.
     * <p>
     * 说明: 添加/删除敏感词后调用此方法.
     */
    public void reload() {
        try {
            // 1. 从数据库加载敏感词
            List<SysSensitiveWord> wordList = wordMapper.selectList(
                Wrappers.<SysSensitiveWord>lambdaQuery()
                    .eq(SysSensitiveWord::getStatus, "ACTIVE")
            );
            List<String> words = wordList.stream()
                .map(SysSensitiveWord::getWord)
                .collect(Collectors.toList());

            // 2. 从数据库加载白名单
            List<SysSensitiveWordWhitelist> whitelistList = whitelistMapper.selectList(null);
            List<String> whitelist = whitelistList.stream()
                .map(SysSensitiveWordWhitelist::getWord)
                .collect(Collectors.toList());

            // 3. 构建 SensitiveWordBs 实例
            this.wordBs = buildSensitiveWordBs();
            this.wordBs.addWord(words);
            this.wordBs.addWordAllow(whitelist);

            log.info("敏感词库刷新完成: 敏感词={}, 白名单={}", words.size(), whitelist.size());
        } catch (Exception e) {
            log.error("敏感词库刷新失败", e);
        }
    }

    /**
     * 构建 SensitiveWordBs 实例.
     * <p>
     * 说明: 统一配置，便于维护.
     *
     * @return SensitiveWordBs 实例
     */
    private SensitiveWordBs buildSensitiveWordBs() {
        return SensitiveWordBs.newInstance()
            // 基础配置
            .enableNumCheck(true)
            .ignoreCase(true)
            .ignoreWidth(true)
            .ignoreNumStyle(true)
            .ignoreChineseStyle(true)
            .ignoreEnglishStyle(true)
            .ignoreRepeat(false)
            // 特殊格式检查
            .enableEmailCheck(true)
            .enableUrlCheck(true)
            .enableIpv4Check(true)
            .enableWordCheck(true)
            // 性能配置
            .wordFailFast(true)
            // 检查器配置
            .wordCheckNum(WordChecks.num())
            .wordCheckEmail(WordChecks.email())
            .wordCheckUrl(WordChecks.url())
            .wordCheckIpv4(WordChecks.ipv4())
            .wordCheckWord(WordChecks.word())
            // 规则配置
            .numCheckLen(11)
            .wordTag(WordTags.none())
            .charIgnore(SensitiveWordCharIgnores.defaults())
            .wordResultCondition(WordResultConditions.alwaysTrue())
            .init();
    }

    /**
     * 检查文本是否包含敏感词.
     *
     * @param text 待检查的文本
     * @return true=包含敏感词 false=不包含
     */
    public boolean contains(String text) {
        return wordBs.contains(text);
    }

    /**
     * 替换敏感词（默认替换为 *）.
     *
     * @param text 待替换的文本
     * @return 替换后的文本
     */
    public String replace(String text) {
        return wordBs.replace(text);
    }

    /**
     * 替换敏感词（自定义替换符）.
     *
     * @param text        待替换的文本
     * @param replaceChar 替换符
     * @return 替换后的文本
     */
    public String replace(String text, char replaceChar) {
        IWordReplace replace = WordReplaces.chars(replaceChar);
        return replace(text, replace);
    }

    /**
     * 替换敏感词（自定义替换策略）.
     *
     * @param text    待替换的文本
     * @param replace 替换策略
     * @return 替换后的文本
     */
    public String replace(String text, IWordReplace replace) {
        SensitiveWordBs customBs = SensitiveWordBs.newInstance()
            .wordReplace(replace)
            .init();
        return customBs.replace(text);
    }

    /**
     * 查找所有敏感词.
     *
     * @param text 待检查的文本
     * @return 敏感词列表
     */
    public List<String> findAll(String text) {
        return wordBs.findAll(text);
    }

    /**
     * 查找第一个敏感词.
     *
     * @param text 待检查的文本
     * @return 第一个敏感词；如果没有则返回 null
     */
    public String findFirst(String text) {
        return wordBs.findFirst(text);
    }

    /**
     * 查找所有敏感词（自定义结果处理器）.
     *
     * @param text    待检查的文本
     * @param handler 结果处理器
     * @return 处理后的结果列表
     */
    public <R> List<R> findAll(String text, com.github.houbb.sensitive.word.api.IWordResultHandler<R> handler) {
        return wordBs.findAll(text, handler);
    }

    /**
     * 查找第一个敏感词（自定义结果处理器）.
     *
     * @param text    待检查的文本
     * @param handler 结果处理器
     * @return 处理后的结果
     */
    public <R> R findFirst(String text, com.github.houbb.sensitive.word.api.IWordResultHandler<R> handler) {
        return wordBs.findFirst(text, handler);
    }
}
