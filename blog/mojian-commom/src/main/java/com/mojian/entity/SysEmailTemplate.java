package com.mojian.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邮件模板实体类
 */
@Data
@TableName("sys_email_template")
@ApiModel(value = "邮件模板对象")
public class SysEmailTemplate implements Serializable {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "模板编码（唯一标识）")
    private String templateCode;

    @ApiModelProperty(value = "模板名称")
    private String templateName;

    @ApiModelProperty(value = "邮件主题")
    private String subject;

    @ApiModelProperty(value = "模板内容（HTML）")
    private String content;

    @ApiModelProperty(value = "模板说明")
    private String remark;

    @ApiModelProperty(value = "状态：0=禁用，1=启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
