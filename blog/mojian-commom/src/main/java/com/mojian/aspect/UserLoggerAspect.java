package com.mojian.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.mojian.annotation.UserLogger;
import com.mojian.common.constant.Constants;
import com.mojian.config.ResponseAdvice;
import com.mojian.dto.user.LoginUserInfo;
import com.mojian.entity.SysOperateLog;
import com.mojian.mapper.SysOperateLogMapper;
import com.mojian.utils.AspectUtil;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 用户端操作日志切面
 * 记录所有用户操作，失败时废弃
 */
@Aspect
@Component
@RequiredArgsConstructor
public class UserLoggerAspect {

    private static final Logger logger = LoggerFactory.getLogger(UserLoggerAspect.class);

    private final SysOperateLogMapper operateLogMapper;

    /**
     * 开始时间（使用ThreadLocal解决多线程问题）
     */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Pointcut(value = "@annotation(userLogger)")
    public void pointcut(UserLogger userLogger) {
    }

    @Around(value = "pointcut(userLogger)")
    public Object doAround(ProceedingJoinPoint joinPoint, UserLogger userLogger) throws Throwable {
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
                handle(joinPoint, userLogger, responseCode, errorMsg);
            } catch (Exception e) {
                logger.error("用户日志记录出错!", e);
            } finally {
                // 清除ThreadLocal
                START_TIME.remove();
            }
        }

        return result;
    }

    /**
     * 日志收集
     */
    private void handle(ProceedingJoinPoint point, UserLogger userLogger, Integer responseCode, String errorMsg) {
        try {
            // 获取request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();

            // 获取当前登录用户
            String username = "匿名用户";
            try {
                LoginUserInfo user = (LoginUserInfo) StpUtil.getSession().get(Constants.CURRENT_USER);
                if (user != null) {
                    username = user.getUsername();
                }
            } catch (Exception e) {
                // 未登录用户
            }

            // 获取方法信息
            Method currentMethod = AspectUtil.INSTANCE.getMethod(point);
            String operationName = userLogger.value();
            String module = userLogger.module();

            // 如果操作名称为空，使用方法名
            if (operationName.isEmpty()) {
                operationName = currentMethod.getName();
            }

            // 获取请求参数
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
                    .type("user")
                    .username(username)
                    .operationName(operationName)
                    .module(module)
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

            // 异步保存日志（不阻塞主线程）
            operateLogMapper.insert(operateLog);

            // 清理 ThreadLocal
            ResponseAdvice.clear();

        } catch (Exception e) {
            logger.error("用户日志记录失败", e);
        }
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

            java.util.HashMap<String, Object> paramMap = new java.util.HashMap<>();
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
