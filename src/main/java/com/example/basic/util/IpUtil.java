package com.example.basic.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP 地址获取工具。
 *
 * <p>支持普通请求和经过代理（Nginx）的请求。
 * Nginx 转发时真实IP通常放在 X-Forwarded-For / X-Real-IP 头中。
 *
 * @author hermes-agent
 */
@Slf4j
@Component
public class IpUtil {

    /** 不可作为 IP 的占位符 */
    private static final String UNKNOWN = "unknown";

    /** Nginx 转发时的真实 IP 头 */
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    /**
     * 获取客户端真实 IP。
     *
     * <p>优先从代理头中获取，层层穿透后第一个非 unknown IP 即为真实 IP。
     *
     * @param request HTTP 请求
     * @return 真实 IP，未获取到返回 "0.0.0.0"
     */
    public static String getRealIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // X-Forwarded-For 可能包含多个IP，取第一个
                if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip.trim();
            }
        }

        // 都没有则取 RemoteAddr
        String ip = request.getRemoteAddr();
        // 本机回环地址转为 127.0.0.1
        if (isLoopback(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    /**
     * 判断 IP 是否有效（非空且非 unknown）。
     */
    private static boolean isValidIp(String ip) {
        return StrUtil.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 判断是否为回环地址（127.0.0.1 / ::1）。
     */
    private static boolean isLoopback(String ip) {
        return "0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip) || "localhost".equals(ip);
    }

    /**
     * 获取服务器本机 IP。
     *
     * <p>用于日志记录，方便排查问题。
     */
    public static String getServerIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
