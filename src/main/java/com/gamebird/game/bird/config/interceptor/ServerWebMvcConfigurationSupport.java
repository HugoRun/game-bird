package com.gamebird.game.bird.config.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gamebird.game.bird.data.ConstData;

/**
 * 拦截器注册配置
 *
 * <p>注意：本类原先继承 {@code WebMvcConfigurationSupport}。该基类会让 Spring Boot 的
 * {@code WebMvcAutoConfiguration} 整体失效，导致 {@code @ConfigurationProperties} 的 MVC 绑定、
 * {@code HttpMessageConverter} 自动配置（{@code @ResponseBody} / JSON 序列化）与默认静态资源映射
 * 全部不生效。当前项目恰好靠手工注册 addResourceHandlers 且暂无 @ResponseBody 用法才未暴露，
 * 属于隐藏缺陷，故改为实现 {@link WebMvcConfigurer} 接口。
 */
@Configuration
public class ServerWebMvcConfigurationSupport implements WebMvcConfigurer {

    /**
     * 提前注入bean
     * @return accountAuthHandlerInterceptor
     */
    @Bean
    public AccountAuthHandlerInterceptor accountAuthHandlerInterceptor() {
        return new AccountAuthHandlerInterceptor();
    }

    /**
     * 提前注入bean
     * @return roleAuthHandlerInterceptor
     */
    @Bean
    public RoleAuthHandlerInterceptor roleAuthHandlerInterceptor() {
        return new RoleAuthHandlerInterceptor();
    }

    /**
     * 提前注入bean
     * @return GameStatusHandlerInterceptor
     */
    @Bean
    public GameStatusHandlerInterceptor gameStatusHandlerInterceptor() {
        return new GameStatusHandlerInterceptor();
    }

    /**
     * 添加自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 运行状态拦截器
        registry.addInterceptor(gameStatusHandlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/favicon.ico", "/error*", ConstData.MESSAGE_PATH);
        // 账号登录路由拦截器
        registry.addInterceptor(accountAuthHandlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/favicon.ico", "/error*");
        // 角色登录路由拦截器
        registry.addInterceptor(roleAuthHandlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/assets/**", "/favicon.ico", "/error*");
        // 如果需要拦截错误信息, 屏蔽掉上面的 , "/error*"
    }

    /**
     * 排除静态资源拦截
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将[static]目录 映射到[/assets/**]路径
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/", "classpath:/META-INF/resources/");
        // 将[/static/favicon.ico]文件 映射到 [/favicon.ico]
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");
    }
}