package com.mojian.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojian.config.interceptor.ApiAccessLogInterceptor;
import com.mojian.entity.SysFileOss;
import com.mojian.enums.FileOssEnum;
import com.mojian.mapper.SysFileOssMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

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

        // 本地存储文件访问
        // 方案1：从数据库读取配置（推荐）
        try {
            SysFileOss sysFileOss = sysFileOssMapper.selectOne(
                    new LambdaQueryWrapper<SysFileOss>()
                            .eq(SysFileOss::getPlatform, FileOssEnum.LOCAL.getValue())
            );

            if (sysFileOss != null && sysFileOss.getPathPatterns() != null && sysFileOss.getStoragePath() != null) {
                System.out.println("========================================");
                System.out.println("注册本地文件资源处理器:");
                System.out.println("  路径模式: " + sysFileOss.getPathPatterns());
                System.out.println("  存储位置: file:" + sysFileOss.getStoragePath());
                System.out.println("========================================");

                registry.addResourceHandler(sysFileOss.getPathPatterns())
                        .addResourceLocations("file:" + sysFileOss.getStoragePath());
            } else {
                System.out.println("警告: 未找到本地存储配置，使用默认配置");
                // 方案2：硬编码默认配置（如果数据库配置为空）
                registry.addResourceHandler("localFile/**")
                        .addResourceLocations("file:D:/Chen/Pictures/dev_temp/");
            }
        } catch (Exception e) {
            System.out.println("错误: 读取本地存储配置失败: " + e.getMessage());
            e.printStackTrace();
            // 出错时使用默认配置
            registry.addResourceHandler("localFile/**")
                    .addResourceLocations("file:D:/Chen/Pictures/dev_temp/");
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiAccessLogInterceptor());
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
}
