package com.mojian.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mojian.config.WeChatConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 微信公众号服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatService {

    private final WeChatConfig weChatConfig;
    private final StringRedisTemplate redisTemplate;

    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info";

    /**
     * 验证微信服务器签名
     *
     * @param signature 微信加密签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @return 验证结果
     */
    public boolean checkSignature(String signature, String timestamp, String nonce) {
        String token = weChatConfig.getToken();
        String[] arr = {token, timestamp, nonce};
        Arrays.sort(arr);

        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s);
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes());
            String hash = bytesToHex(digest);
            return hash.equals(signature);
        } catch (NoSuchAlgorithmException e) {
            log.error("签名验证失败", e);
            return false;
        }
    }

    /**
     * 生成登录验证码
     *
     * @return 验证码
     */
    public String generateLoginCode() {
        int length = weChatConfig.getCodeLength();
        String code = RandomUtil.randomNumbers(length);

        // 存储到 Redis，设置过期时间
        String key = WeChatConfig.LOGIN_CODE_KEY + code;
        redisTemplate.opsForValue().set(key, code, weChatConfig.getCodeExpire(), TimeUnit.SECONDS);

        log.info("生成登录验证码: {}", code);
        return code;
    }

    /**
     * 验证登录验证码
     *
     * @param code 用户输入的验证码
     * @return 验证结果
     */
    public boolean verifyLoginCode(String code) {
        String key = WeChatConfig.LOGIN_CODE_KEY + code;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            // 验证成功，删除验证码
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    /**
     * 获取 Access Token
     *
     * @return access_token
     */
    public String getAccessToken() {
        // 先从缓存获取
        String cachedToken = redisTemplate.opsForValue().get(WeChatConfig.ACCESS_TOKEN_KEY);
        if (cachedToken != null) {
            return cachedToken;
        }

        // 缓存不存在，重新获取
        String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                ACCESS_TOKEN_URL, weChatConfig.getAppId(), weChatConfig.getSecret());

        try {
            HttpResponse response = HttpRequest.get(url).execute();
            JSONObject json = JSONUtil.parseObj(response.body());

            if (json.containsKey("access_token")) {
                String accessToken = json.getStr("access_token");
                int expiresIn = json.getInt("expires_in");

                // 缓存 Token，提前 5 分钟过期
                redisTemplate.opsForValue().set(
                        WeChatConfig.ACCESS_TOKEN_KEY,
                        accessToken,
                        expiresIn - 300,
                        TimeUnit.SECONDS
                );

                return accessToken;
            } else {
                log.error("获取 Access Token 失败: {}", json);
                return null;
            }
        } catch (Exception e) {
            log.error("获取 Access Token 异常", e);
            return null;
        }
    }

    /**
     * 获取用户信息
     *
     * @param openId 用户 OpenID
     * @return 用户信息
     */
    public JSONObject getUserInfo(String openId) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            return null;
        }

        String url = String.format("%s?access_token=%s&openid=%s&lang=zh_CN",
                USER_INFO_URL, accessToken, openId);

        try {
            HttpResponse response = HttpRequest.get(url).execute();
            return JSONUtil.parseObj(response.body());
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return null;
        }
    }

    /**
     * 处理接收到的文本消息
     *
     * @param fromUser 发送者 OpenID
     * @param content  消息内容
     * @return 回复消息
     */
    public String handleTextMessage(String fromUser, String content) {
        // 去除空格
        content = content.trim();

        // 验证是否是登录验证码
        if (verifyLoginCode(content)) {
            // 验证成功，存储用户会话
            String sessionKey = WeChatConfig.LOGIN_SESSION_KEY + content;
            redisTemplate.opsForValue().set(sessionKey, fromUser, 5, TimeUnit.MINUTES);

            return "✅ 验证成功！请返回网页完成登录。";
        }

        // 默认回复
        return "欢迎关注！如需登录博客，请发送网页上显示的验证码。";
    }

    /**
     * 检查用户是否验证成功
     *
     * @param code 验证码
     * @return 用户 OpenID，未验证返回 null
     */
    public String checkLoginStatus(String code) {
        String sessionKey = WeChatConfig.LOGIN_SESSION_KEY + code;
        String openId = redisTemplate.opsForValue().get(sessionKey);
        if (openId != null) {
            // 验证成功，删除会话
            redisTemplate.delete(sessionKey);
        }
        return openId;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
