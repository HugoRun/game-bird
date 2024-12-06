package com.qq2008.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CommonUtils {
    /***
     * 获取当前时间戳(秒级)
     * @return 当前时间戳(秒级)
     */
    public static Integer nowTime() {
        return (int) (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
    }

    public static Integer nowDate() {
        return (int) (LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.of("+8")));
    }

    /***
     * 随机字符串
     * @param len 随机长度
     * @param strRange 字符范围
     * @return 随机到的字符串
     */
    public static String randStr(int len, String strRange) {
        if (len == 0) {
            return "";
        }
        if (strRange.trim().isEmpty()) {
            return "";
        }

        StringBuilder buffer = new StringBuilder();
        int strLen = strRange.length();
        for (int i = 0; i < len; i++) {
            buffer.append(strRange.charAt((int) (Math.random() * strLen)));
        }
        return buffer.toString();
    }


    /***
     * 随机速记证书
     * @return 随机到的数值
     */
    public static Integer randNum(int minNum, int maxNum) {
        return (int) (Math.random() * (maxNum - minNum + 1) + minNum);
    }

    public static void main(String[] args) {
        System.out.println("nowTime:" + nowTime());
        System.out.println("nowDate:" + nowDate());
    }
}
