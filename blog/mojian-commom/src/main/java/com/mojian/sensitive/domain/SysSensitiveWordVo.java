package com.mojian.sensitive.domain;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 系统敏感词视图对象 sys_sensitive_word
 *
 * @author Lion Li
 * @date 2026-07-07
 */
@Data
public class SysSensitiveWordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
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
