package com.example.basic.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS 跨站脚本攻击过滤器。
 *
 * <p>将请求参数中的 HTML / JavaScript 特殊字符转义，
 * 防止用户输入的恶意脚本被浏览器解析执行。
 *
 * <p>使用 Spring 的 HiddenHttpMethodFilter 包装原始 request，
 * 保证 POST + _method=DELETE/PUT 仍然正确过滤。
 *
 * @author hermes-agent
 */
@Component
@WebFilter(urlPatterns = "/*")
@Order(1)
public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 无需初始化资源
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    @Override
    public void destroy() {
        // 无需释放资源
    }
}
