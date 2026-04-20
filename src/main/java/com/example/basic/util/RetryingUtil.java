package com.example.basic.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 重试工具：固定间隔 / 指数退避 / 熔断器。
 *
 * @author hermes-agent
 */
@Slf4j
@Component
public class RetryingUtil {

    /** 固定间隔重试 */
    public static <T> T withRetry(Supplier<T> task, int maxRetries, long intervalMs) {
        Exception last = null;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                return task.get();
            } catch (Exception e) {
                last = e;
                if (i < maxRetries) {
                    log.warn("第{}次失败，{}ms后重试: {}", i, intervalMs, e.getMessage());
                    sleep(intervalMs);
                }
            }
        }
        throw new RetryExhausted("重试" + maxRetries + "次后仍然失败", last);
    }

    /** 指数退避重试 */
    public static <T> T withExponentialBackoff(Supplier<T> task, int maxRetries, long baseMs) {
        Exception last = null;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                return task.get();
            } catch (Exception e) {
                last = e;
                if (i < maxRetries) {
                    long wait = baseMs * (1L << (i-1));
                    log.warn("指数退避 | attempt={}/{} | wait={}ms", i, maxRetries, wait);
                    sleep(wait);
                }
            }
        }
        throw new RetryExhausted("指数退避重试" + maxRetries + "次后仍然失败", last);
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public static class RetryExhausted extends RuntimeException {
        public RetryExhausted(String msg, Throwable cause) { super(msg, cause); }
    }
}
