package com.example.shop.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 业务单号生成器（与 seed.sql 演示数据格式一致）
 * - 订单号：JD + yyyyMMddHHmmss + 3 位序号
 * - 退款单号：RF 前缀
 * - 支付流水号：PAY 前缀
 * 序号每毫秒内自增（0-999），同毫秒并发安全
 */
public final class IdGenerator {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static volatile String lastTs = "";

    private IdGenerator() {
    }

    private static String next(String prefix) {
        String ts = LocalDateTime.now().format(TS);
        synchronized (IdGenerator.class) {
            if (!ts.equals(lastTs)) {
                lastTs = ts;
                SEQ.set(0);
            }
        }
        int seq = SEQ.getAndIncrement() % 1000;
        return String.format("%s%s%03d", prefix, ts, seq);
    }

    /** 订单编号：JD+时间戳+序号（例：JD20260812103000123） */
    public static String orderNo() {
        return next("JD");
    }

    /** 退款单编号：RF+时间戳+序号 */
    public static String refundNo() {
        return next("RF");
    }

    /** 支付流水号：PAY+时间戳+序号 */
    public static String paymentNo() {
        return next("PAY");
    }
}
