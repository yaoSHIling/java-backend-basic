package com.example.basic.modules.notification.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.basic.modules.notification.entity.Notification;
import com.example.basic.modules.notification.service.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * 多渠道通知服务实现。
 *
 * <p>支持：钉钉 / Server酱 / 飞书 / 企业微信 / 邮件。
 * 配置即启用，环境变量驱动。
 *
 * @author hermes-agent
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Value("${notification.dingtalk.enabled:false}")   private boolean dingtalkEnabled;
    @Value("${notification.dingtalk.webhook-url:}")   private String dingtalkWebhook;
    @Value("${notification.serverchan.enabled:false}") private boolean serverchanEnabled;
    @Value("${notification.serverchan.sendkey:}")       private String serverchanSendkey;
    @Value("${notification.feishu.enabled:false}")      private boolean feishuEnabled;
    @Value("${notification.feishu.webhook-url:}")       private String feishuWebhook;
    @Value("${notification.wecom.enabled:false}")     private boolean wecomEnabled;
    @Value("${notification.wecom.webhook-url:}")       private String wecomWebhook;
    @Value("${notification.mail.enabled:false}")      private boolean mailEnabled;
    @Value("${notification.mail.host:}")               private String mailHost;
    @Value("${notification.mail.username:}")           private String mailUsername;
    @Value("${notification.mail.password:}")           private String mailPassword;
    @Value("${notification.mail.from:}")               private String mailFrom;

    @PostConstruct
    public void init() {
        log.info("通知服务 | 钉钉={} | Server酱={} | 飞书={} | 企业微信={} | 邮件={}",
                dingtalkEnabled, serverchanEnabled, feishuEnabled, wecomEnabled, mailEnabled);
    }

    @Override
    public boolean send(Notification n) {
        if (n == null || StrUtil.isBlank(n.getContent())) return false;
        String ch = StrUtil.blankToDefault(n.getChannel(), "dingtalk");
        try {
            return switch (ch.toLowerCase()) {
                case "dingtalk", "dingding" -> sendDingTalk(n);
                case "serverchan", "pushplus" -> sendServerChan(n);
                case "feishu", "lark" -> sendFeiShu(n);
                case "wecom", "wechatwork" -> sendWeCom(n);
                case "email", "mail" -> sendMail(n);
                default -> sendAll(n);
            };
        } catch (Exception e) {
            log.error("通知发送失败 | channel={} | error={}", ch, e.getMessage());
            return false;
        }
    }

    @Override
    public void send(String ch, String title, String content) {
        send(Notification.builder().channel(ch).title(title).content(content).build());
    }

    @Override
    public void sendDefault(String title, String content) { send("dingtalk", title, content); }

    @Override
    public void sendAlert(String title, String content) {
        send(Notification.builder().channel("dingtalk")
                .title("🔴 " + title).content(content).level("error").build());
    }

    // 钉钉
    private boolean sendDingTalk(Notification n) {
        if (!dingtalkEnabled || StrUtil.isBlank(dingtalkWebhook)) return false;
        String body = JSON.toJSONString(Map.of("msgtype", "text", "text", Map.of("content", buildText(n))));
        String resp = HttpRequest.post(dingtalkWebhook)
                .header("Content-Type", "application/json").body(body).timeout(10000).execute().body();
        JSONObject r = JSON.parseObject(resp);
        boolean ok = r.getIntValue("errcode") == 0;
        log.info("钉钉通知 | success={} | title={}", ok, n.getTitle());
        return ok;
    }

    // Server 酱
    private boolean sendServerChan(Notification n) {
        if (!serverchanEnabled || StrUtil.isBlank(serverchanSendkey)) return false;
        String url = "https://sctapi.ftqq.com/" + serverchanSendkey + ".send";
        String body = JSON.toJSONString(Map.of(
                "title", StrUtil.blankToDefault(n.getTitle(), "通知"),
                "desp", n.getContent()));
        String resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(body).timeout(10000).execute().body();
        JSONObject r = JSON.parseObject(resp);
        boolean ok = r.getIntValue("code") == 0;
        log.info("Server酱通知 | success={} | title={}", ok, n.getTitle());
        return ok;
    }

    // 飞书
    private boolean sendFeiShu(Notification n) {
        if (!feishuEnabled || StrUtil.isBlank(feishuWebhook)) return false;
        String body = JSON.toJSONString(Map.of("msg_type", "text", "content", Map.of("text", buildText(n))));
        String resp = HttpRequest.post(feishuWebhook).header("Content-Type", "application/json")
                .body(body).timeout(10000).execute().body();
        JSONObject r = JSON.parseObject(resp);
        boolean ok = r.getIntValue("code") == 0;
        log.info("飞书通知 | success={} | title={}", ok, n.getTitle());
        return ok;
    }

    // 企业微信
    private boolean sendWeCom(Notification n) {
        if (!wecomEnabled || StrUtil.isBlank(wecomWebhook)) return false;
        String body = JSON.toJSONString(Map.of("msgtype", "text", "text", Map.of("content", buildText(n))));
        String resp = HttpRequest.post(wecomWebhook).header("Content-Type", "application/json")
                .body(body).timeout(10000).execute().body();
        JSONObject r = JSON.parseObject(resp);
        boolean ok = r.getIntValue("errcode") == 0;
        log.info("企业微信通知 | success={} | title={}", ok, n.getTitle());
        return ok;
    }

    // 邮件
    private boolean sendMail(Notification n) {
        if (!mailEnabled || StrUtil.isBlank(mailHost)) return false;
        try {
            cn.hutool.extra.mail.MailUtil.send(
                    mailFrom, StrUtil.blankToDefault(n.getTo(), mailUsername),
                    n.getTitle(), n.getContent(), false);
            log.info("邮件通知成功 | to={} | title={}", n.getTo(), n.getTitle());
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
            return false;
        }
    }

    // 全渠道
    private boolean sendAll(Notification n) {
        if (sendDingTalk(n)) return true;
        if (sendServerChan(n)) return true;
        if (sendFeiShu(n)) return true;
        if (sendWeCom(n)) return true;
        if (sendMail(n)) return true;
        log.warn("所有通知渠道均失败 | title={}", n.getTitle());
        return false;
    }

    private String buildText(Notification n) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(n.getTitle())) sb.append(n.getTitle()).append("\n\n");
        sb.append(n.getContent());
        if ("error".equals(n.getLevel())) sb.append("\n⚠️ [ERROR]");
        return sb.toString();
    }
}
