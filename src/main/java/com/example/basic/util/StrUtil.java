package com.example.basic.util;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil as HS;
import cn.hutool.core.util.URLUtil;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 字符串工具（封装 Hutool StrUtil）。
 *
 * <p>核心原则：永远不返回 null，返回空字符串 ""。
 *
 * @author hermes-agent
 */
public class StrUtil {

    public static boolean isEmpty(CharSequence s)      { return HS.isEmpty(s); }
    public static boolean isNotEmpty(CharSequence s)  { return !isEmpty(s); }
    public static boolean isBlank(CharSequence s)      { return HS.isBlank(s); }
    public static boolean isNotBlank(CharSequence s)    { return !isBlank(s); }
    public static String ifEmpty(String s, String def) { return isEmpty(s) ? def : s; }
    public static String ifBlank(String s, String def)  { return isBlank(s) ? def : s; }
    public static String trim(String s)                 { return s != null ? s.trim() : ""; }
    public static String trimAll(String s)              { return isEmpty(s) ? "" : s.replaceAll("\\s+", ""); }
    public static String lowerFirst(String s)           { return HS.lowerFirst(s); }
    public static String upperFirst(String s)           { return HS.upperFirst(s); }
    public static String toCamelCase(String s)        { return HS.toCamelCase(s); }
    public static String toUnderlineCase(String s)     { return HS.toUnderlineCase(s); }
    public static String replace(String s, String f, String r) { return s.replace(f, r); }
    public static boolean contains(CharSequence s, CharSequence sub) { return HS.contains(s, sub); }
    public static String join(String delim, Object... parts) { return HS.join(delim, parts); }
    public static String repeat(String s, int n)       { return HS.repeat(s, n); }
    public static String urlEncode(String s)           { return URLUtil.encode(s, StandardCharsets.UTF_8); }
    public static String urlDecode(String s)           { return URLUtil.decode(s, StandardCharsets.UTF_8); }

    public static String substring(String s, int start, int end) {
        return HS.sub(s, start, end);
    }

    public static String left(String s, int len) {
        if (isEmpty(s) || len <= 0) return "";
        return s.substring(0, Math.min(len, s.length()));
    }

    public static String right(String s, int len) {
        if (isEmpty(s) || len <= 0) return "";
        return s.substring(s.length() - Math.min(len, s.length()));
    }

    /** 占位符格式化：StrUtil.format("你好，{}！", "世界") */
    public static String format(String tpl, Object... params) {
        if (isBlank(tpl)) return tpl;
        return StrFormatter.format(tpl, params);
    }

    /** Map 占位符：StrUtil.format("用户${name}登录了", Map.of("name", "admin")) */
    public static String format(String tpl, Map<String, ?> params) {
        if (isBlank(tpl) || params == null || params.isEmpty()) return tpl;
        for (Map.Entry<String, ?> e : params.entrySet()) {
            tpl = tpl.replace("${" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return tpl;
    }

    /** 手机号脱敏：138****8888 */
    public static String maskPhone(String p) {
        if (isBlank(p) || p.length() < 7) return p;
        return p.substring(0, 3) + "****" + p.substring(7);
    }

    /** 邮箱脱敏：ab***@gmail.com */
    public static String maskEmail(String e) {
        if (isBlank(e) || !e.contains("@")) return e;
        String[] p = e.split("@", 2);
        if (p[0].length() <= 2) return e;
        return p[0].charAt(0) + "***" + p[0].charAt(p[0].length()-1) + "@" + p[1];
    }
}
