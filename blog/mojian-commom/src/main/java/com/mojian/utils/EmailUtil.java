package com.mojian.utils;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojian.common.RedisConstants;
import com.mojian.entity.SysWebConfig;
import com.mojian.mapper.SysWebConfigMapper;
import com.mojian.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: quequnlong
 * @date: 2024/12/28
 * @description: 邮箱工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtil {

    @Value("${mail.smtp.email}")
    private String fromEmail;

    @Value("${mail.smtp.password}")
    private String password;

    @Value("${mail.smtp.port}")
    private int port;

    @Value("${mail.smtp.host}")
    private String host;

    private final RedisUtil redisUtil;

    private final EmailTemplateService emailTemplateService;

    private final SysWebConfigMapper sysWebConfigMapper;

    private final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    public void getJavaMailSenderImpl() {
        javaMailSender.setHost(host);
        javaMailSender.setUsername(fromEmail);
        javaMailSender.setPassword(password);
        javaMailSender.setPort(port);
        javaMailSender.setDefaultEncoding("UTF-8");
        Properties p = new Properties();
        p.setProperty("mail.smtp.auth", "true");
        p.setProperty("mail.debug", "true");
        javaMailSender.setJavaMailProperties(p);
    }

    /**
     * 获取网站配置
     */
    private SysWebConfig getWebConfig() {
        return sysWebConfigMapper.selectOne(
                new LambdaQueryWrapper<SysWebConfig>()
                        .last("limit 1"));
    }

    /**
     * 发送验证码
     *
     * @param email 邮箱地址
     * @throws MessagingException 发送异常
     */
    public void sendCode(String email) throws MessagingException {
        this.getJavaMailSenderImpl();

        // 生成验证码
        String code = RandomUtil.randomString(5);

        // 获取网站配置
        SysWebConfig webConfig = getWebConfig();
        String siteName = webConfig != null ? webConfig.getName() : "拾壹博客";
        String siteUrl = webConfig != null ? webConfig.getWebUrl() : "https://www.shiyit.com";
        String logoUrl = webConfig != null ? webConfig.getLogo() : "";

        // 准备模板变量
        Map<String, String> variables = new HashMap<>();
        variables.put("code", code);
        variables.put("siteName", siteName);
        variables.put("siteUrl", siteUrl);
        variables.put("logoUrl", logoUrl);

        // 从数据库获取模板并渲染
        String content = emailTemplateService.renderTemplate("verify_code", variables);

        // 如果数据库没有模板，使用默认模板
        if (content == null) {
            content = getDefaultTemplate(variables);
            log.warn("使用默认邮件模板，建议在后台配置邮件模板");
        }

        // 发送邮件
        String subject = "您有一封来自 " + siteName + " 的回执！";
        this.send(email, subject, content);
        log.info("邮箱验证码发送成功,邮箱:{},验证码:{}", email, code);

        // 保存验证码到Redis
        redisUtil.set(RedisConstants.CAPTCHA_CODE_KEY + email, code);
        redisUtil.expire(RedisConstants.CAPTCHA_CODE_KEY + email, RedisConstants.MINUTE_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 获取默认模板（当数据库没有模板时使用）
     */
    private String getDefaultTemplate(Map<String, String> variables) {
        String siteName = variables.getOrDefault("siteName", "拾壹博客");
        String siteUrl = variables.getOrDefault("siteUrl", "https://www.shiyit.com");
        String logoUrl = variables.getOrDefault("logoUrl", "");
        String code = variables.getOrDefault("code", "");

        return "<html>\n" +
                "<body>\n" +
                "<div style=\"position:relative;font-size:14px;height:auto;padding:15px;line-height:1.7;\">\n" +
                "  <div style=\"max-width:800px;margin:20px auto 0 auto;\">\n" +
                "    <table cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #fff;border-collapse: collapse; border:1px solid #e5e5e5;box-shadow: 0 10px 15px rgba(0, 0, 0, 0.05);text-align: left;width: 100%;font-size: 14px;\">\n" +
                "      <tbody>\n" +
                "        <tr style=\"background-color: #f8f8f8;\">\n" +
                "          <td>\n" +
                "            <img style=\"padding: 15px 0 15px 30px;width:50px\" src=\"" + logoUrl + "\" />\n" +
                "            <span>" + siteName + "</span>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding: 30px;\">\n" +
                "            <h1 style=\"font-size: 26px; font-weight: bold;\">验证您的邮箱地址</h1>\n" +
                "            <p style=\"line-height:1.75em;\">感谢您使用 " + siteName + ". </p>\n" +
                "            <p style=\"line-height:1.75em;\">以下是您的邮箱验证码，请将它输入到 <span style=\"color:#409eff;\">" + siteName + "</span> 的邮箱验证码输入框中:</p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding: 0 30px;\">\n" +
                "            <p style=\"color: #253858;text-align:center;line-height:1.75em;background-color: #f2f2f2;min-width: 200px;margin: 0 auto;font-size: 28px;border-radius: 5px;border: 1px solid #d9d9d9;font-weight: bold;\">" + code + "</p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding: 30px;\">\n" +
                "            <p style=\"line-height:1.75em;\">这一封邮件包括一些您的私密信息，请不要回复或转发它，以免带来不必要的信息泄露风险。 </p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td style=\"padding: 30px;\">\n" +
                "            <hr>\n" +
                "            <p style=\"text-align: center;line-height:1.75em;\">" + siteName + " - <a href='" + siteUrl + "' style='text-decoration: none;color:#409eff'>" + siteName + "</a></p>\n" +
                "          </td>\n" +
                "        </tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "  </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 发送邮件
     *
     * @param email   邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容（HTML）
     * @throws MessagingException 发送异常
     */
    private void send(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mineHelper = new MimeMessageHelper(mimeMessage, true);
        mineHelper.setSubject(subject);
        mineHelper.setFrom(Objects.requireNonNull(javaMailSender.getUsername()));
        mineHelper.setTo(email);
        mineHelper.setSentDate(DateUtil.getNowDate());
        mineHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }
}
