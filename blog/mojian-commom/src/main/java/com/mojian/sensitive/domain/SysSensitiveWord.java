package com.mojian.sensitive.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统敏感词对象 sys_sensitive_word
 *
 * @author Lion Li
 * @date 2026-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sensitive_word")
public class SysSensitiveWord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 敏感词内容
     */
    private String word;

    /**
     * 类型: CUSTOM-自定义 OFFICIAL-官方
     */
    private String wordType;

    /**
     * 状态: ACTIVE-启用 DISABLED-禁用
     */
    private String status;


}
