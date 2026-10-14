package com.qq2008.game.bird.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器配置
 */
@Configuration
public class ServerConfig {

    /***
     * 构造跨域配置
     *
     * <p>【RT-6 安全项】改造前硬编码 addAllowedOrigin("*") / addAllowedHeader("*") /
     * addAllowedMethod("*")，等价于对全网开放。本项目是 Thymeleaf 服务端渲染单体，
     * 没有任何跨域调用需求，因此改为「配置驱动 + 默认全禁」：只有
     * {@code game.cors.allowed-origins} 显式列出时才开放。
     *
     * <p>注意：本项目不使用跨域凭证（Session Cookie 同源），故不设置
     * allowCredentials —— 否则 Spring 会拒绝搭配 allowedOrigins 中的 "*"。
     *
     * @param props CORS 白名单配置
     * @return CorsConfiguration
     */
    private CorsConfiguration buildCorsConfig(GameCorsProperties props) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        List<String> origins = props.getAllowedOrigins();
        if (origins.isEmpty()) {
            // 无白名单：不开放任何跨域源。CORS 只在跨域时生效，同源请求不受影响。
            corsConfiguration.setAllowedOrigins(List.of());
        } else {
            corsConfiguration.setAllowedOrigins(origins);
            corsConfiguration.addAllowedHeader("*");
            // Spring 6 的 CorsConfiguration 只有单参 addAllowedMethod，没有 varargs 重载
            corsConfiguration.addAllowedMethod("GET");
            corsConfiguration.addAllowedMethod("POST");
        }
        return corsConfiguration;
    }

    /***
     * 跨域配置
     *
     * @param props CORS 白名单配置
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter(GameCorsProperties props) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildCorsConfig(props));
        return new CorsFilter(source);
    }

    /***
     * Thymeleaf全局变量
     * @param resolver resolver
     */
    @Resource
    private void configureThymeleafStaticVars(ThymeleafViewResolver resolver) {
        if (resolver != null) {
            Map<String, Object> vars = new HashMap<>();
            resolver.setStaticVariables(vars);
        }
    }

}
