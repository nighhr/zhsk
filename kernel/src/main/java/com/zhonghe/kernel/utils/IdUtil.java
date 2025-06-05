package com.zhonghe.kernel.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class IdUtil {
    /**
     * 生成24位订单ID (格式: 年月日时分秒+6位随机数+4位序列)
     * 示例: 20230520153045123456
     */
    public static String orderId() {
        // 时间部分 (18位)
        String timePart = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        // 随机数部分 (6位)
        String randomPart = String.format("%06d",
                ThreadLocalRandom.current().nextInt(999999));

        return timePart.substring(0, 14) + randomPart;
    }
}