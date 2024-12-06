package com.qq2008.game.bird.config.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * 拦截器注册配置
 */
@Configuration
public class ServerWebMvcConfigurationSupport extends WebMvcConfigurationSupport {

    /***
     * 提前注入bean
     * @return accountAuthHandlerInterceptor
     */
    @Bean
    public AccountAuthHandlerInterceptor accountAuthHandlerInterceptor() {
        return new AccountAuthHandlerInterceptor();
    }

    /***
     * 提前注入bean
     * @return roleAuthHandlerInterceptor
     */
    @Bean
    public RoleAuthHandlerInterceptor roleAuthHandlerInterceptor() {
        return new RoleAuthHandlerInterceptor();
    }

    /***
     * 提前注入bean
     * @return GameStatusHandlerInterceptor
     */
    @Bean
    public GameStatusHandlerInterceptor gameStatusHandlerInterceptor() {
        return new GameStatusHandlerInterceptor();
    }

    /***
     * 添加自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 运行状态拦截器
        registry.addInterceptor(gameStatusHandlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/favicon.ico", "/error*", "/message");
        // 账号登录路由拦截器
        registry.addInterceptor(accountAuthHandlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/favicon.ico", "/error*");
        // 角色登录路由拦截器
        registry.addInterceptor(roleAuthHandlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/favicon.ico", "/error*");
        // 如果需要拦截错误信息, 屏蔽掉上面的 , "/error*"
        super.addInterceptors(registry);
    }

    /***
     * 排除静态资源拦截
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将[static]目录 映射到[/assets/**]路径
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/", "classpath:/META-INF/resources/");
        // 将[/static/favicon.ico]文件 映射到 [/favicon.ico]
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");
        super.addResourceHandlers(registry);
    }
}