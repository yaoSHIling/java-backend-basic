package com.example.basic.modules.workflow.engine;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量解析器
 *
 * <p>核心功能：
 * <ul>
 *   <li>{{ variable }} 模板渲染：将字符串模板中的变量替换为实际值</li>
 *   <li>变量类型推断与转换</li>
 *   <li>变量来源合并（全局变量 + 输入数据 + 上游输出）</li>
 *   <li>JSONPath 表达式求值</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * VariableResolver resolver = new VariableResolver(context.getAllVariables());
 * resolver.put("name", "张三");
 * String result = resolver.render("您好，{{ name }}"); // "您好，张三"
 * </pre>
 */
@Slf4j
public class VariableResolver {

    /** 模板变量占位符：{{ variable }} 或 {{ nested.var }} */
    private static final Pattern TEMPLATE_PATTERN =
        Pattern.compile("\{\{\s*([\w.\[\]]+)\s*\}\}");

    /** 上下文变量 */
    private final Map<String, Object> context;

    public VariableResolver() {
        this.context = new LinkedHashMap<>();
    }

    public VariableResolver(Map<String, Object> initialContext) {
        this.context = new LinkedHashMap<>(initialContext);
    }

    // ==================== 增删改查 ====================

    public void put(String key, Object value) {
        this.context.put(key, value);
    }

    public Object get(String key) {
        return resolveValue(key);
    }

    public Object get(String key, Object defaultValue) {
        Object v = resolveValue(key);
        return v != null ? v : defaultValue;
    }

    public Map<String, Object> getAll() {
        return new LinkedHashMap<>(context);
    }

    public void merge(Map<String, Object> other) {
        if (other == null) return;
        other.forEach(context::put);
    }

    // ==================== 模板渲染 ====================

    /**
     * 渲染模板字符串，将 {{ variable }} 替换为实际值
     *
     * @param template 如："您好，{{ name }}，您的申请金额为 {{ amount }} 元"
     * @return 渲染后的字符串
     */
    public String render(String template) {
        if (template == null) return null;

        String result = template;
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String expr = matcher.group(1).trim();
            Object value = resolveValue(expr);
            String replacement = value != null ? String.valueOf(value) : "";
            // 避免 $ 字符导致 StringBuffer 出问题
            matcher.appendReplacement(sb,
                Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 批量渲染 Map 中的所有字符串值
     */
    public Map<String, Object> renderMap(Map<String, Object> input) {
        if (input == null) return new LinkedHashMap<>();

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : input.entrySet()) {
            Object value = e.getValue();
            if (value instanceof String) {
                value = render((String) value);
            } else if (value instanceof Map) {
                value = renderMap((Map<String, Object>) value);
            } else if (value instanceof List) {
                value = renderList((List<Object>) value);
            }
            result.put(e.getKey(), value);
        }
        return result;
    }

    /**
     * 批量渲染 List 中的所有字符串值
     */
    public List<Object> renderList(List<Object> input) {
        if (input == null) return new ArrayList<>();
        List<Object> result = new ArrayList<>();
        for (Object item : input) {
            if (item instanceof String) {
                result.add(render((String) item));
            } else if (item instanceof Map) {
                result.add(renderMap((Map<String, Object>) item));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    // ==================== JSONPath 求值 ====================

    /**
     * 支持 {{ users[0].name }} 或 {{ data[0]["key"] }} 访问
     */
    private Object resolveValue(String expr) {
        if (expr == null || expr.isEmpty()) return null;

        // 纯变量名
        if (!expr.contains("[") && !expr.contains(".") && !expr.contains(""")) {
            return context.get(expr);
        }

        // 数组下标或嵌套属性访问
        try {
            return evalJsonPath(expr);
        } catch (Exception e) {
            log.warn("变量解析失败 | expr={} | error={}", expr, e.getMessage());
            return null;
        }
    }

    private Object evalJsonPath(String expr) {
        // 简化版 JSONPath：支持 a[0].b 或 a[0]["b"]
        String[] parts = expr.split("[.\[\]"]+");
        Object current = null;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) continue;

            if (current == null) {
                // 第一个部分从 context 找
                current = context.get(part);
            } else if (current instanceof List) {
                // 数组下标
                int index = Integer.parseInt(part);
                List<?> list = (List<?>) current;
                current = index < list.size() ? list.get(index) : null;
            } else if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                // 反射或其他方式
                current = null;
            }

            if (current == null) break;
        }

        return current;
    }

    // ==================== 类型转换 ====================

    /** 转为 Integer */
    public Integer asInt(String key, Integer defaultValue) {
        Object v = get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch {
            return defaultValue;
        }
    }

    /** 转为 Double */
    public Double asDouble(String key, Double defaultValue) {
        Object v = get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch {
            return defaultValue;
        }
    }

    /** 转为 Boolean */
    public Boolean asBool(String key, Boolean defaultValue) {
        Object v = get(key);
        if (v == null) return defaultValue;
        if (v instanceof Boolean) return (Boolean) v;
        String s = String.valueOf(v).toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s);
    }

    /** 转为 String */
    public String asStr(String key) {
        Object v = get(key);
        return v != null ? String.valueOf(v) : null;
    }

    /** 判断变量是否存在 */
    public boolean contains(String key) {
        return resolveValue(key) != null;
    }

    /** 导出为 JSON 字符串 */
    public String toJson() {
        return JSONUtil.toJsonStr(context);
    }

    @Override
    public String toString() {
        return "VariableResolver" + context;
    }
}
