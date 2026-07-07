package com.mojian.sensitive.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 敏感词白名单对象 sys_sensitive_word_whitelist
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sensitive_word_whitelist")
public class SysSensitiveWordWhitelist extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 白名单词
     */
    private String word;

    /**
     * 添加原因
     */
    private String reason;
}
