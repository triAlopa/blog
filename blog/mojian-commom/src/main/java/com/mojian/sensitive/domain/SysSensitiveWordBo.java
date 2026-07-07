package com.mojian.sensitive.domain;


import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 系统敏感词业务对象 sys_sensitive_word
 *
 * @author Lion Li
 * @date 2026-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysSensitiveWordBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空" )
    private Long id;

    /**
     * 敏感词内容
     */
    @NotBlank(message = "敏感词内容不能为空" )
    private String word;

    /**
     * 类型: CUSTOM-自定义 OFFICIAL-官方
     */
    @NotBlank(message = "类型: CUSTOM-自定义 OFFICIAL-官方不能为空" )
    private String wordType;

    /**
     * 状态: ACTIVE-启用 DISABLED-禁用
     */
    @NotBlank(message = "状态: ACTIVE-启用 DISABLED-禁用不能为空" )
    private String status;


}
