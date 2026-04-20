package com.example.basic.util;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import java.io.OutputStream;

/**
 * 验证码工具（图形验证码/算术验证码/短信验证码）。
 *
 * @author hermes-agent
 */
@Component
public class VerifyCodeUtil {

    public static final String SESSION_KEY = "verify_code";
    public static final String SESSION_KEY_EXPIRE = "verify_code_expire";

    /** 生成字母数字图片验证码 */
    public static String createImageCode(int w, int h, int len, HttpSession session, OutputStream out) {
        RandomGenerator gen = new RandomGenerator("ABCDEFGHJKLMNPQRSTUVWXYZ23456789", len);
        var captcha = CaptchaUtil.createCircleCaptcha(w, h);
        captcha.setGenerator(gen);
        String code = captcha.getCode();
        session.setAttribute(SESSION_KEY, code);
        session.setAttribute(SESSION_KEY_EXPIRE, System.currentTimeMillis() + 5*60*1000);
        captcha.write(out);
        return code;
    }

    /** 生成算术验证码（更友好，不怕 OCR） */
    public static String createMathCode(HttpSession session, OutputStream out) {
        var captcha = CaptchaUtil.createMathCaptcha(120, 40);
        captcha.setGenerator(new MathGenerator());
        captcha.write(out);
        String code = captcha.getCode(); // 如 "3+5=?"
        String answer = String.valueOf(evalMath(code));
        session.setAttribute(SESSION_KEY, answer);
        session.setAttribute(SESSION_KEY_EXPIRE, System.currentTimeMillis() + 5*60*1000);
        return answer;
    }

    private static int evalMath(String ex) {
        ex = ex.replaceAll("\\s+", "").replace("=?", "");
        if (ex.contains("+")) {
            String[] p = ex.split("\\+", 2);
            return Integer.parseInt(p[0].trim()) + Integer.parseInt(p[1].trim());
        }
        if (ex.contains("-")) {
            String[] p = ex.split("-", 2);
            return Integer.parseInt(p[0].trim()) - Integer.parseInt(p[1].trim());
        }
        return Integer.parseInt(ex);
    }

    /** 生成短信验证码 */
    public static String createSmsCode(int len) { return SecureUtil.randCode(len); }

    /** 验证验证码 */
    public static boolean verify(String inp, HttpSession session) {
        if (inp == null || inp.isBlank()) return false;
        Long expire = (Long) session.getAttribute(SESSION_KEY_EXPIRE);
        if (expire == null || System.currentTimeMillis() > expire) {
            session.removeAttribute(SESSION_KEY);
            session.removeAttribute(SESSION_KEY_EXPIRE);
            return false;
        }
        String stored = (String) session.getAttribute(SESSION_KEY);
        session.removeAttribute(SESSION_KEY);
        session.removeAttribute(SESSION_KEY_EXPIRE);
        if (stored == null) return false;
        return stored.equalsIgnoreCase(inp);
    }
}
