package com.qq2008.game.bird.config.interceptor;

import com.qq2008.common.annotation.SkipAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.logging.Logger;

/***
 * 账号登录认证拦截器
 * 对需要登录账号才能访问的路由进行拦截
 */
public class AccountAuthHandlerInterceptor implements HandlerInterceptor {

    // 日志句柄
    Logger logger = Logger.getLogger(AccountAuthHandlerInterceptor.class.getName());

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 方法拦截器且未拥有SkipAccount的注解
        if (handler instanceof HandlerMethod handlerMethod && handlerMethod.getMethodAnnotation(SkipAccount.class) == null) {
            String methodPath = handlerMethod.getMethod().getDeclaringClass() + "." + handlerMethod.getMethod().getName();
            // 检测session中是否存储账号信息, 未存储直接跳转到登录页
            HttpSession session = request.getSession();
            if (null == session.getAttribute("account")) {
                logger.info(methodPath + "未拥有SkipAccount注解，进行账号校验！");
                response.sendRedirect(request.getContextPath() + "/login.do");
                return false;
            }
        }
        return true;
    }
}
