package com.qq2008.game.bird.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS 白名单配置。
 *
 * <p><b>默认空列表 = 不开放任何跨域源</b>，浏览器同源策略直接生效。
 * 改造前是硬编码 {@code addAllowedOrigin("*")}，等价于对全网开放，
 * 而本项目是 Thymeleaf 服务端渲染单体，根本没有跨域调用需求。
 *
 * <p><b>为什么只保留 List 重载</b>：Spring Boot 的 relaxed binding 本身支持把
 * {@code game.cors.allowed-origins=https://a.com,https://b.com} 这样的逗号分隔
 * 字符串自动绑定成 {@code List<String>}。若额外提供 {@code setAllowedOrigins(String)}
 * 重载，绑定器会在两个 setter 之间产生歧义，反而绑定失败。故不提供该重载。
 */
@Component
@ConfigurationProperties(prefix = "game.cors")
public class GameCorsProperties {

    /** 允许的来源列表；空表示不开放跨域。 */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * 获取允许的来源列表。
     *
     * @return 允许的来源列表；空列表表示不开放跨域
     */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * 设置允许的来源列表。
     *
     * @param allowedOrigins 允许的来源列表；null 视为空列表
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = (allowedOrigins == null) ? new ArrayList<>() : allowedOrigins;
    }
}