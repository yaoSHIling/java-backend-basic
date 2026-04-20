package com.example.basic.filter;

import cn.hutool.core.util.EscapeUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * XSS 过滤请求包装器。
 *
 * <p>包装原始 HttpServletRequest，重写：
 * <ul>
 *   <li>{@link #getParameter(String)} — 参数值自动转义</li>
 *   <li>{@link #getParameterValues(String)}</li>
 *   <li>{@link #getHeader(String)} — Header 值自动转义</li>
 *   <li>{@link #getInputStream()} — POST Body 中的参数也转义</li>
 * </ul>
 *
 * <p>配合 {@link XssFilter} 使用，过滤所有进入 Controller 的请求参数。
 *
 * @author hermes-agent
 * @see XssFilter
 * @see XssUtil
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return escape(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        return java.util.Arrays.stream(values)
                .map(this::escape)
                .toArray(String[]::new);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return escape(value);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 读取原始 Body
        InputStream in = super.getInputStream();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        // 转义后写回
        String escapedBody = escape(sb.toString());
        byte[] bytes = escapedBody.getBytes(StandardCharsets.UTF_8);

        return new ServletInputStream() {
            private final ByteArrayInputStream byteArrayInputStream =
                    new ByteArrayInputStream(bytes);

            @Override
            public boolean isFinished() { return byteArrayInputStream.available() == 0; }

            @Override
            public boolean isReady() { return true; }

            @Override
            public void setReadListener(ReadListener listener) { /* noop */ }

            @Override
            public int read() { return byteArrayInputStream.read(); }
        };
    }

    /** 对字符串进行 XSS 转义 */
    private String escape(String input) {
        return EscapeUtil.escapeHtml4(input);
    }
}
