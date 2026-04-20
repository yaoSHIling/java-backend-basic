package com.example.basic.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置。
 *
 * <p>使用 ThreadPoolExecutor 而非直接用 Executors，
 * 以便显式控制队列容量（Executors 默认队列无限大，可能导致 OOM）。
 *
 * <p>提供两类线程池：
 * <ul>
 *   <li>{@code taskExecutor} — 通用异步任务（@Async 默认使用）</li>
 *   <li>{@code ioIntensiveExecutor} — IO 密集型任务（文件/网络读写）</li>
 * </ul>
 */
@Configuration
public class ThreadPoolConfig {

    // ========== 通用异步线程池（CPU + IO 混合型）==========

    /**
     * 核心参数说明：
     * - 核心线程数 = CPU 核心数（通常设置为 CPU 核数）
     * - 最大线程数 = CPU 核心数 * 2（应对突发流量）
     * - 队列容量 = 100（超过的请求进入队列，队列满则触发拒绝策略）
     * - 空闲线程存活时间 = 60s（释放多余线程）
     */
    @Bean("taskExecutor")
    public ThreadPoolExecutor taskExecutor() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                cpuCores,                  // 核心线程数
                cpuCores * 2,              // 最大线程数（突发流量）
                60L, TimeUnit.SECONDS,     // 空闲线程存活时间
                new LinkedBlockingQueue<>(100),  // 有界队列，防止无限堆积
                Executors.defaultThreadFactory(), // 默认线程工厂
                // 拒绝策略：超过队列容量时由调用线程执行（同步执行，不丢弃任务）
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // ========== IO 密集型线程池（适合文件上传/网络请求）==========

    /**
     * IO 密集型线程池：线程数可以设大一些（CPU 核心数的 2~3 倍），
     * 因为 IO 等待时线程会释放 CPU，CPU 不会饱和。
     */
    @Bean("ioIntensiveExecutor")
    public ThreadPoolExecutor ioIntensiveExecutor() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
                cpuCores * 2,
                cpuCores * 4,
                120L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
