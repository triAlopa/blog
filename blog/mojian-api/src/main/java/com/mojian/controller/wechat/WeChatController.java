package com.mojian.controller.wechat;

import com.mojian.common.Result;
import com.mojian.service.WeChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信公众号控制器
 */
@Slf4j
//@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
@Api(tags = "微信公众号")
public class WeChatController {

    private final WeChatService weChatService;
    private final com.mojian.config.WeChatConfig weChatConfig;

    /**
     * 微信服务器验证（GET请求）
     * 配置服务器时微信会调用此接口验证有效性
     */
    @GetMapping("/callback")
    public String verify(
            @RequestParam String signature,
            @RequestParam String timestamp,
            @RequestParam String nonce,
            @RequestParam String echostr) {

        log.info("收到微信验证请求: signature={}, timestamp={}, nonce={}", signature, timestamp, nonce);

        if (weChatService.checkSignature(signature, timestamp, nonce)) {
            log.info("微信签名验证成功");
            return echostr;
        }

        log.warn("微信签名验证失败");
        return "fail";
    }

    /**
     * 接收微信消息（POST请求）
     * 用户发送消息时微信会推送到此接口
     */
    @PostMapping("/callback")
    public String handleMessage(HttpServletRequest request) {
        try {
            // 读取请求体中的 XML
            String xmlBody = readRequestBody(request);
            log.info("收到微信消息: {}", xmlBody);

            // 手动解析 XML（兼容 Java 8）
            String fromUser = extractXmlValue(xmlBody, "FromUserName");
            String toUser = extractXmlValue(xmlBody, "ToUserName");
            String msgType = extractXmlValue(xmlBody, "MsgType");
            String content = extractXmlValue(xmlBody, "Content");

            // 处理文本消息
            if ("text".equals(msgType)) {
                String reply = weChatService.handleTextMessage(fromUser, content);
                return buildReplyXml(fromUser, toUser, reply);
            }

            // 关注事件
            if ("event".equals(msgType)) {
                String eventType = extractXmlValue(xmlBody, "Event");
                if ("subscribe".equals(eventType)) {
                    return buildReplyXml(fromUser, toUser,
                            "🎉 欢迎关注！\n\n如需登录博客，请在网页上获取验证码并发送给我。");
                }
            }

            // 默认回复
            return buildReplyXml(fromUser, toUser, "感谢关注！如需登录博客，请发送验证码。");

        } catch (Exception e) {
            log.error("处理微信消息异常", e);
            return "success";
        }
    }

    /**
     * 从 XML 中提取指定标签的值
     */
    private String extractXmlValue(String xml, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";

        // 处理 CDATA 格式
        String cdataStart = "<" + tagName + "><![CDATA[";
        String cdataEnd = "]]></" + tagName + ">";

        int cdataStartIdx = xml.indexOf(cdataStart);
        if (cdataStartIdx >= 0) {
            int cdataEndIdx = xml.indexOf(cdataEnd, cdataStartIdx);
            if (cdataEndIdx >= 0) {
                return xml.substring(cdataStartIdx + cdataStart.length(), cdataEndIdx);
            }
        }

        int startIdx = xml.indexOf(startTag);
        if (startIdx >= 0) {
            int endIdx = xml.indexOf(endTag, startIdx);
            if (endIdx >= 0) {
                return xml.substring(startIdx + startTag.length(), endIdx);
            }
        }

        return null;
    }

    /**
     * 生成登录二维码数据
     */
    @GetMapping("/login/qrcode")
    @ApiOperation("获取微信登录二维码")
    public Result<Map<String, String>> getLoginQrcode() {
        String code = weChatService.generateLoginCode();
        String appId = weChatConfig.getAppId();

        // 生成公众号二维码 URL（用户需要扫码关注）
        // 注意：这里使用公众号的关注链接，实际生产环境可以使用带参数的二维码
        String qrcodeUrl = "https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=" + appId;

        Map<String, String> result = new HashMap<>(4);
        result.put("code", code);
        result.put("qrcodeUrl", qrcodeUrl);
        result.put("expire", String.valueOf(300));

        return Result.success(result);
    }

    /**
     * 轮询检查登录状态
     */
    @GetMapping("/login/check")
    @ApiOperation("检查微信登录状态")
    public Result<String> checkLoginStatus(@RequestParam String code) {
        String openId = weChatService.checkLoginStatus(code);

        if (openId != null) {
            // TODO: 调用登录服务，获取用户信息并生成 Token
            return Result.success("登录成功");
        }

        return Result.success(null);
    }

    /**
     * 读取请求体
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * 构建回复 XML
     */
    private String buildReplyXml(String toUser, String fromUser, String content) {
        return String.format("""
                <xml>
                  <ToUserName><![CDATA[%s]]></ToUserName>
                  <FromUserName><![CDATA[%s]]></FromUserName>
                  <CreateTime>%d</CreateTime>
                  <MsgType><![CDATA[text]]></MsgType>
                  <Content><![CDATA[%s]]></Content>
                </xml>
                """, toUser, fromUser, System.currentTimeMillis() / 1000, content);
    }
}
