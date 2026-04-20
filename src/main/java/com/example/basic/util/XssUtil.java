package com.example.basic.util;

import cn.hutool.core.util.EscapeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * XSS（跨站脚本攻击）防护工具。
 *
 * <p>将 HTML/JavaScript 特殊字符转义为安全字符：
 * <pre>
 * & → &amp;amp;    < → &amp;lt;    > → &amp;gt;
 * " → &amp;quot;    ' → &amp;#x27;  / → &amp;#x2F;
 * </pre>
 *
 * <p>输入被转义后，即使包含 <script>alert('xss')</script>，
 * 也会被浏览器当作纯文本显示，而非可执行脚本。
 *
 * <p>实际使用见 {@link com.example.basic.filter.XssRequestWrapper}。
 *
 * @author hermes-agent
 */
@Slf4j
@Component
public class XssUtil {

    /**
     * 对字符串进行 XSS 转义（HTML 特殊字符转义）。
     *
     * @param input 原始字符串
     * @return 转义后的安全字符串
     */
    public static String escape(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return EscapeUtil.escapeHtml4(input);
    }

    /**
     * 还原 XSS 转义（用于展示时还原原始内容）。
     *
     * @param escaped 已转义的字符串
     * @return 原始字符串
     */
    public static String unescape(String escaped) {
        if (escaped == null || escaped.isEmpty()) {
            return escaped;
        }
        return EscapeUtil.unescapeHtml4(escaped);
    }

    /**
     * 判断字符串是否包含 HTML 标签（可能有 XSS 风险）。
     *
     * @param input 待检测字符串
     * @return true=包含HTML标签，false=纯文本
     */
    public static boolean containsHtml(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.matches(".*<[^>]+>.*");
    }
}
