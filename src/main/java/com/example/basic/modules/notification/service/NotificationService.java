package com.example.basic.modules.notification.service;

import com.example.basic.modules.notification.entity.Notification;

/**
 * 统一通知服务接口。
 *
 * <p>支持：钉钉群机器人 / Server酱 / 飞书 / 企业微信 / 邮件。
 *
 * @author hermes-agent
 */
public interface NotificationService {

    /** 发送通知 */
    boolean send(Notification notification);

    /** 快捷发送 */
    void send(String channel, String title, String content);

    /** 使用默认渠道发送 */
    void sendDefault(String title, String content);

    /** 发送系统告警 */
    void sendAlert(String title, String content);
}
