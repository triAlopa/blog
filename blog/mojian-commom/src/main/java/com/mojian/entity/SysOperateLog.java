package com.mojian.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mojian.utils.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 操作日志实体类
 */
@Data
@Builder
@TableName("sys_operate_log")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "操作日志对象")
public class SysOperateLog implements Serializable {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "日志类型：admin=管理端, user=用户端")
    private String type;

    @ApiModelProperty(value = "操作用户")
    private String username;

    @ApiModelProperty(value = "操作名称")
    private String operationName;

    @ApiModelProperty(value = "模块名称")
    private String module;

    @ApiModelProperty(value = "请求接口")
    private String requestUrl;

    @ApiModelProperty(value = "请求方式（GET/POST/PUT/DELETE）")
    private String requestMethod;

    @ApiModelProperty(value = "请求参数")
    private String requestParams;

    @ApiModelProperty(value = "响应状态码")
    private Integer responseCode;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

    @ApiModelProperty(value = "IP地址")
    private String ip;

    @ApiModelProperty(value = "IP来源（省份/城市）")
    private String ipSource;

    @ApiModelProperty(value = "User-Agent")
    private String userAgent;

    @ApiModelProperty(value = "设备类型（PC/Mobile/Tablet）")
    private String deviceType;

    @ApiModelProperty(value = "操作系统")
    private String os;

    @ApiModelProperty(value = "浏览器类型")
    private String browser;

    @ApiModelProperty(value = "请求耗时（毫秒）")
    private Long spendTime;

    @ApiModelProperty(value = "类路径")
    private String classPath;

    @ApiModelProperty(value = "方法名")
    private String methodName;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = DateUtil.YYYY_MM_DD_HH_MM_SS, timezone = "GMT+8")
    private LocalDateTime createTime;
}
