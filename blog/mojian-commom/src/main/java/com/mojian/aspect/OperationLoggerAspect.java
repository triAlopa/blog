package com.mojian.aspect;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.mojian.annotation.OperationLogger;
import com.mojian.common.constant.Constants;
import com.mojian.config.ResponseAdvice;
import com.mojian.dto.user.LoginUserInfo;
import com.mojian.entity.SysOperateLog;
import com.mojian.mapper.SysOperateLogMapper;
import com.mojian.utils.AspectUtil;
import com.mojian.utils.DateUtil;
import com.mojian.utils.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * 管理端日志切面
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLoggerAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLoggerAspect.class);

    private final SysOperateLogMapper operateLogMapper;

    /**
     * 开始时间（使用ThreadLocal解决多线程问题）
     */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Pointcut(value = "@annotation(operationLogger)")
    public void pointcut(OperationLogger operationLogger) {

    }

    @Around(value = "pointcut(operationLogger)")
    public Object doAround(ProceedingJoinPoint joinPoint, OperationLogger operationLogger) throws Throwable {
        HttpServletRequest request = IpUtil.getRequest();
        StpUtil.checkLogin();
        //因给了演示账号所有权限以供用户观看，所以执行业务前需判断是否是管理员操作
        if (!StpUtil.hasRole(Constants.ADMIN)) {
            throw new NotPermissionException("无权限");
        }

        // 记录开始时间
        START_TIME.set(System.currentTimeMillis());

        Object result = null;
        Integer responseCode = 200;
        String errorMsg = null;

        try {
            // 执行业务
            result = joinPoint.proceed();
        } catch (Exception e) {
            // 记录错误信息
            responseCode = 500;
            errorMsg = e.getMessage();
            throw e; // 继续抛出异常
        } finally {
            // 记录日志（无论成功失败都记录）
            try {
                handle(joinPoint, request, operationLogger, responseCode, errorMsg);
            } catch (Exception e) {
                logger.error("管理端日志记录出错!", e);
            } finally {
                // 清除ThreadLocal
                START_TIME.remove();
            }
        }

        return result;
    }

    /**
     * 管理员日志收集
     */
    private void handle(ProceedingJoinPoint point, HttpServletRequest request,
                        OperationLogger annotation, Integer responseCode, String errorMsg) {

        boolean save = annotation.save();
        String operationName = AspectUtil.INSTANCE.parseParams(point.getArgs(), annotation.value());
        if (!save) {
            return;
        }

        // 获取当前操作用户
        LoginUserInfo user = (LoginUserInfo) StpUtil.getSession().get(Constants.CURRENT_USER);

        // 获取参数JSON
        String paramsJson = getParamsJson(point);

        // 获取请求信息
        String requestMethod = request.getMethod();
        String requestUrl = request.getRequestURI();
        String ip = IpUtil.getIp();
        String ipSource = IpUtil.getIp2region(ip);
        String userAgentStr = request.getHeader("User-Agent");

        // 解析User-Agent
        String deviceType = "PC";
        String os = "Unknown";
        String browser = "Unknown";
        if (userAgentStr != null && !userAgentStr.isEmpty()) {
            try {
                UserAgent userAgent = UserAgent.parseUserAgentString(userAgentStr);
                deviceType = getDeviceType(userAgentStr);
                os = userAgent.getOperatingSystem().getName();
                browser = userAgent.getBrowser().getName();
            } catch (Exception e) {
                // 解析失败使用默认值
            }
        }

        // 计算耗时
        Long startTime = START_TIME.get();
        Long spendTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

        // 计算耗时等级：<=200ms=fast, 200-1000ms=normal, >1000ms=slow
        String durationLevel = "fast";
        if (spendTime > 1000) {
            durationLevel = "slow";
        } else if (spendTime > 200) {
            durationLevel = "normal";
        }

        // 获取响应体
        String responseBody = null;
        try {
            responseBody = ResponseAdvice.getResponseBody();
        } catch (Exception e) {
            // ignore
        }

        // 构建日志对象
        SysOperateLog operateLog = SysOperateLog.builder()
                .type("admin")
                .username(user.getUsername())
                .operationName(operationName)
                .module(annotation.module())
                .requestUrl(requestUrl)
                .requestMethod(requestMethod)
                .requestParams(paramsJson)
                .responseCode(responseCode)
                .errorMsg(errorMsg)
                .ip(ip)
                .ipSource(ipSource)
                .userAgent(userAgentStr)
                .deviceType(deviceType)
                .os(os)
                .browser(browser)
                .spendTime(spendTime)
                .durationLevel(durationLevel)
                .responseBody(responseBody)
                .classPath(point.getTarget().getClass().getName())
                .methodName(point.getSignature().getName())
                .createTime(LocalDateTime.now())
                .build();

        operateLogMapper.insert(operateLog);

        // 清理 ThreadLocal
        ResponseAdvice.clear();
    }

    /**
     * 获取请求参数JSON
     */
    private String getParamsJson(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            String[] parameterNames = methodSignature.getParameterNames();

            HashMap<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < parameterNames.length; i++) {
                // 过滤掉request和response对象
                if (!(args[i] instanceof HttpServletRequest) && !(args[i] instanceof HttpServletResponse)) {
                    paramMap.put(parameterNames[i], args[i]);
                }
            }

            return JSONUtil.toJsonStr(paramMap);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 根据User-Agent判断设备类型
     */
    private String getDeviceType(String userAgent) {
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else {
            return "PC";
        }
    }
}
