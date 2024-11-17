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
import java.util.Map;

/**
 * 服务器配置
 */
@Configuration
public class ServerConfig {

    /***
     * 构造跨域配置
     * @return CorsConfiguration
     */
    private CorsConfiguration buildCorsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许任何域名使用
        corsConfiguration.addAllowedOrigin("*");
        // 允许任何头
        corsConfiguration.addAllowedHeader("*");
        // 允许任何方法（post、get等）
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    /***
     * 跨域配置
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildCorsConfig());
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
