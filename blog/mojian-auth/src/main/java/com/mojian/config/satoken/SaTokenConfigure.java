package com.mojian.config.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 * 注意：拦截器配置已移至 WebMvcConfig.java 中统一管理
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
  /*  // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，定义详细的拦截路由
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/logout",
                        "/auth/verify",
                        "/swagger-ui/**",          // knife4j接口文档
                        "/webjars/**",        // knife4j相关资源
                        "/v3/api-docs/**",     // openapi接口文档
                        "/doc.html",     // openapi接口文档
                        "/favicon.ico",     // openapi接口文档
                        "/swagger-resources",
                        "/api/**",
                        "/wechat/**",
                        "/localFile/**"
                );
    }*/
}
