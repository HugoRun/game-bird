package com.qq2008.game.bird.config.interceptor;

import com.qq2008.common.annotation.SkipRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.logging.Logger;

/***
 * 角色登录认证拦截器
 * 对需要登录角色才能访问的路由进行拦截
 */
public class RoleAuthHandlerInterceptor implements HandlerInterceptor {

    // 日志句柄
    Logger logger = Logger.getLogger(RoleAuthHandlerInterceptor.class.getName());

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 方法拦截器并且未拥有@SkipRole注解
        if (handler instanceof HandlerMethod handlerMethod && handlerMethod.getMethod().getAnnotation(SkipRole.class) == null) {
            String methodPath = handlerMethod.getMethod().getDeclaringClass() + "." + handlerMethod.getMethod().getName();
            // 检测session中是否存储角色信息, 如果未存储则跳转到角色列表
            HttpSession session = request.getSession();
            if (null == session.getAttribute("sRole")) {
                logger.info(methodPath + "未拥有SkipRole注解，进行账号校验！");
                response.sendRedirect(request.getContextPath() + "/role/login.do");
                return false;
            }
        }
        return true;
    }
}
