package com.qq2008.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/***
 * JSON工具
 */
public class JSONUtils {

    // ObjectMapper对象
    private static final ObjectMapper mapper = new ObjectMapper();

    /***
     * 格式化JSON
     * @param obj 对象
     * @return JSON文本
     */
    public static String toJSON(Object obj) {
        try {
            return "\r\n" + mapper.writeValueAsString(obj) + "\r\n";
            //return obj.getClass() + "——> \r\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj) + "\r\n";
        } catch (JsonProcessingException e) {
            // throw new RuntimeException(e);
        }
        return "";
    }
}
