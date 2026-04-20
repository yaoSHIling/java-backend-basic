package com.example.basic.util;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 雪花算法 ID 生成工具。
 *
 * <p>生成全局唯一、趋势递增的 64 位整数 ID，
 * 比 UUID 更短、更有序（支持数据库索引）。
 *
 * <p>适用于：订单号、流水号、消息ID等需要全局唯一的场景。
 *
 * <p>MyBatis-Plus 的 ID_TYPE.AUTO 模式默认使用雪花算法。
 *
 * @author hermes-agent
 */
@Slf4j
@Component
public class SnowflakeIdUtil {

    private static final Sequence SEQUENCE = new Sequence();

    /**
     * 生成下一个唯一 ID。
     *
     * @return 64位整数ID
     */
    public static long nextId() {
        return SEQUENCE.nextId();
    }

    /**
     * 生成带前缀的字符串ID（如 "ORDER_" + 19位数字）。
     *
     * @param prefix 前缀
     * @return 带前缀的字符串ID
     */
    public static String nextIdWithPrefix(String prefix) {
        return prefix + nextId();
    }

    /**
     * 从 ID 中提取时间戳（雪花ID高位是时间戳）。
     *
     * @param id 雪花ID
     * @return 时间戳（毫秒）
     */
    public static long extractTimestamp(long id) {
        return id >> 22 /* 22 = workerIdBits + dataCenterIdBits */ ;
    }
}
