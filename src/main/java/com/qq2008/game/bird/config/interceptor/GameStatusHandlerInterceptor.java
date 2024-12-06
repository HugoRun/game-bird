package com.qq2008.game.bird.config.interceptor;

import com.qq2008.common.entiy.vo.MessageVO;
import com.qq2008.game.bird.data.GameServer;
import com.qq2008.game.bird.model.vo.GameStatusVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.logging.Logger;

/***
 * 游戏状态拦截器
 * 当游戏处于维护状态时禁止访问
 */
public class GameStatusHandlerInterceptor implements HandlerInterceptor {

    // 日志句柄
    Logger logger = Logger.getLogger(GameStatusHandlerInterceptor.class.getName());

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 方法拦截器且游戏不在运行中
        if (handler instanceof HandlerMethod && GameServer.getStatus() != GameStatusVO.RUNNING) {
            request.setAttribute("message", MessageVO.error("游戏正在维护中, 请稍后重新尝试访问。"));
            request.getRequestDispatcher("/message").forward(request, response);
            return false;
        }
        return true;
    }
}
