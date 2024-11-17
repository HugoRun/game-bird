package com.qq2008.common.util;

public class CommonUtils {
    /***
     * 获取当前时间戳(秒级)
     * @return 当前时间戳(秒级)
     */
    public static Integer nowTimestamp() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /***
     * 随机字符串
     * @param len 随机长度
     * @param strRange 字符范围
     * @return 随机到的字符串
     */
    public static String randStr(int len, String strRange){
        if(len == 0){
            return "";
        }
        if(strRange.trim().isEmpty()){
            return "";
        }

        StringBuilder buffer = new StringBuilder();
        int strLen = strRange.length();
        for(int i = 0 ; i < len ; i++){
            buffer.append(strRange.charAt((int) (Math.random() * strLen)));
        }
        return buffer.toString();
    }

    public static void main(String[] args) {
        System.out.println(CommonUtils.randStr(6, "abcdefghijklmnopkrstuvwxyzABCDEFGHIJKLMNOPKRSTUVWXYZ0123456789"));
    }
}
