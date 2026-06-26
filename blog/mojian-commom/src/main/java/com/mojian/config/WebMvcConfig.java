package com.mojian.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojian.config.interceptor.ApiAccessLogInterceptor;
import com.mojian.entity.SysFileOss;
import com.mojian.enums.FileOssEnum;
import com.mojian.mapper.SysFileOssMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author blue
 * @date 2022/3/10
 * @apiNote
 */
@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final SysFileOssMapper sysFileOssMapper;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Knife4j 文档资源
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 本地存储文件访问 - 直接硬编码配置，确保生效
        System.out.println("========================================");
        System.out.println("注册本地文件资源处理器:");
        System.out.println("  路径模式: /localFile/**");
        System.out.println("  存储位置: file:D:/Chen/Pictures/dev_temp/");
        System.out.println("========================================");

        // 直接注册本地文件资源处理器，不依赖数据库配置
        // 注意：路径必须以斜杠开头
        registry.addResourceHandler("/localFile/**")
                .addResourceLocations("file:D:/Chen/Pictures/dev_temp/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // API访问日志拦截器
        registry.addInterceptor(new ApiAccessLogInterceptor());

        // Sa-Token 拦截器 - 排除本地文件路径和错误页面
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/logout",
                        "/auth/verify",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/favicon.ico",
                        "/swagger-resources",
                        "/api/**",
                        "/wechat/**",
                        "/localFile/**",
                        "/error"
                );
    }

    /**
     * 注册跨域信息
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // 允许所有跨域地址
                .allowedHeaders("*")
                .allowedMethods("*")
                .maxAge(3600);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 本地文件访问过滤器 - 在拦截器之前处理本地文件请求
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> localFileFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String requestURI = request.getRequestURI();
                // 如果是本地文件请求，检查文件是否存在
                if (requestURI.startsWith("/localFile/")) {
                    // 尝试获取资源，如果资源处理器能找到文件，会自动处理
                    // 这里直接放行，让资源处理器处理
                    filterChain.doFilter(request, response);
                    return;
                }
                filterChain.doFilter(request, response);
            }
        });
        registrationBean.addUrlPatterns("/localFile/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // 最高优先级
        return registrationBean;
    }
}
