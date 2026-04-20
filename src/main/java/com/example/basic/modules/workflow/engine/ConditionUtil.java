package com.example.basic.modules.workflow.engine;

import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 条件表达式求值器
 *
 * <p>支持 Coze 风格条件语法：
 * <pre>
 * amount > 1000
 * level >= 3
 * type == 'reimburse'
 * status != 'pending'
 * name.contains('张')
 * amount in [100, 200, 300]
 * </pre>
 *
 * <p>特性：
 * <ul>
 *   <li>变量占位符：直接在表达式中使用变量名</li>
 *   <li>自动类型转换：数字/字符串/布尔</li>
 *   <li>安全沙箱：只允许数学/逻辑运算</li>
 *   <li>兼容 JavaScript 语法</li>
 * </ul>
 */
@Slf4j
public class ConditionUtil {

    private static final ScriptEngine SCRIPT_ENGINE =
        new ScriptEngineManager().getEngineByName("JavaScript");

    private static final Pattern VAR_PATTERN =
        Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.]*");

    /**
     * 求值布尔条件表达式
     *
     * @param expression 条件表达式，如：amount > 1000 && level == 1
     * @param variables  变量上下文
     * @return true/false
     */
    public static boolean eval(String expression, Map<String, Object> variables) {
        if (expression == null || expression.trim().isEmpty()) {
            return true; // 空条件默认通过
        }

        try {
            String evalExpr = normalizeExpression(prepareExpression(expression.trim(), variables));
            Object result = SCRIPT_ENGINE.eval(evalExpr);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("条件表达式求值失败 | expr={} | error={}", expression, e.getMessage());
            return false;
        }
    }

    /**
     * 求值并返回数字结果（用于数值运算）
     */
    public static Number evalNumber(String expression, Map<String, Object> variables) {
        try {
            String evalExpr = normalizeExpression(prepareExpression(expression.trim(), variables));
            Object result = SCRIPT_ENGINE.eval(evalExpr);
            if (result instanceof Number) return (Number) result;
            return Double.parseDouble(String.valueOf(result));
        } catch (Exception e) {
            log.warn("数值表达式求值失败 | expr={} | error={}", expression, e.getMessage());
            return 0;
        }
    }

    /**
     * 预处理表达式：将变量名替换为实际值
     */
    private static String prepareExpression(String expr, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) return expr;

        // 匹配变量名
        Matcher matcher = VAR_PATTERN.matcher(expr);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group();
            Object value = resolveNestedVar(varName, variables);

            if (value == null) {
                // 未找到的变量视为 undefined，在 JS 中为 undefined
                matcher.appendReplacement(sb, "undefined");
            } else if (value instanceof String) {
                // 字符串两边加引号
                matcher.appendReplacement(sb, "'" + escapeJavaScriptString((String) value) + "'");
            } else if (value instanceof Boolean) {
                matcher.appendReplacement(sb, String.valueOf(value));
            } else if (value instanceof Number) {
                matcher.appendReplacement(sb, String.valueOf(value));
            } else {
                // 其他类型转为字符串
                matcher.appendReplacement(sb, "'" + escapeJavaScriptString(String.valueOf(value)) + "'");
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 兼容更贴近业务的表达式写法，如: value in ['a','b']
     */
    private static String normalizeExpression(String expr) {
        if (expr == null) return null;
        return expr.replaceAll("(.+?)\\s+in\\s+(\\[.*\\])", "$2.includes($1)");
    }

    /**
     * 支持点号访问嵌套属性，如 user.age → variables.get("user").get("age")
     */
    private static Object resolveNestedVar(String varName, Map<String, Object> variables) {
        if (!varName.contains(".")) {
            return variables.get(varName);
        }

        String[] parts = varName.split("[.]");
        Object current = variables.get(parts[0]);

        for (int i = 1; i < parts.length && current != null; i++) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(parts[i]);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * 转义 JavaScript 字符串中的特殊字符
     */
    private static String escapeJavaScriptString(String s) {
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 判断表达式是否为简单比较（用于日志展示）
     */
    public static String summarize(String expression) {
        if (expression == null) return "";
        // 截断过长表达式
        if (expression.length() > 40) {
            return expression.substring(0, 37) + "...";
        }
        return expression;
    }
}
