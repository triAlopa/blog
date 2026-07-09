package com.mojian.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信公众号配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WeChatConfig {

    /**
     * 公众号AppID
     */
    private String appId;

    /**
     * 公众号AppSecret
     */
    private String secret;

    /**
     * 公众号Token（用于消息验证）
     */
    private String token;

    /**
     * 公众号消息加解密密钥
     */
    private String aesKey;

    /**
     * 验证码长度
     */
    private int codeLength = 6;

    /**
     * 验证码有效期（秒）
     */
    private int codeExpire = 300;

    /**
     * Access Token 缓存 Key
     */
    public static final String ACCESS_TOKEN_KEY = "wechat:access_token";

    /**
     * 登录验证码缓存 Key 前缀
     */
    public static final String LOGIN_CODE_KEY = "wechat:login:code:";

    /**
     * 用户会话缓存 Key 前缀
     */
    public static final String LOGIN_SESSION_KEY = "wechat:login:session:";
}
