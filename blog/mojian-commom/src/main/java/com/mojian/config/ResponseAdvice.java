package com.mojian.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应体增强
 * 用于捕获响应内容，供日志记录使用
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    /**
     * ThreadLocal 存储响应体
     */
    private static final ThreadLocal<String> RESPONSE_BODY = new ThreadLocal<>();

    public static String getResponseBody() {
        return RESPONSE_BODY.get();
    }

    public static void clear() {
        RESPONSE_BODY.remove();
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        try {
            // 只记录 JSON 响应，且大小不超过 10KB
            if (selectedContentType.isCompatibleWith(MediaType.APPLICATION_JSON) && body != null) {
                String json = objectMapper.writeValueAsString(body);
                if (json.length() <= 10240) {
                    RESPONSE_BODY.set(json);
                } else {
                    RESPONSE_BODY.set("{\"msg\": \"response too large (>10KB)\"}");
                }
            }
        } catch (Exception e) {
            log.debug("记录响应体失败", e);
        }
        return body;
    }
}
