package com.qq2008.game.bird.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.qq2008.common.annotation.SkipRole;
import com.qq2008.game.bird.data.ConstData;

/**
 * 角色登录认证拦截器
 * 对需要登录角色才能访问的路由进行拦截
 */
public class RoleAuthHandlerInterceptor implements HandlerInterceptor {

    // 日志句柄
    private static final Logger logger = LoggerFactory.getLogger(RoleAuthHandlerInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 方法拦截器并且未拥有@SkipRole注解
        if (handler instanceof HandlerMethod handlerMethod && handlerMethod.getMethod().getAnnotation(SkipRole.class) == null) {
            String methodPath = handlerMethod.getMethod().getDeclaringClass() + "." + handlerMethod.getMethod().getName();
            // 检测session中是否存储角色信息, 如果未存储则跳转到角色列表
            HttpSession session = request.getSession();
            if (null == session.getAttribute(ConstData.SESSION_ROLE_KEY)) {
                logger.info(methodPath + " 未拥有 @SkipRole 注解，进行角色校验！");
                response.sendRedirect(request.getContextPath() + "/role/login");
                return false;
            }
        }
        return true;
    }
}
