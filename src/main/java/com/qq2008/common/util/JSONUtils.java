package com.qq2008.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON工具
 */
public class JSONUtils {

    private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

    // ObjectMapper对象
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 格式化JSON
     * @param obj 对象
     * @return JSON文本
     */
    public static String toJSON(Object obj) {
        try {
            return "\r\n" + mapper.writeValueAsString(obj) + "\r\n";
            //return obj.getClass() + "——> \r\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj) + "\r\n";
        } catch (JsonProcessingException e) {
            // 保留返回 "" 的行为不变：调用方依赖空串表示「序列化失败」，
            // 改成抛异常会让它们的行为发生变化。但静默吞掉异常会让排查时
            // 完全看不到线索（原实现连被注释的 throw 都没启用），故补 error 日志。
            logger.error("JSON 序列化失败, objType={}", obj == null ? "null" : obj.getClass().getName(), e);
        }
        return "";
    }
}
