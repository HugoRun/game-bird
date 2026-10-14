package com.qq2008.game.bird.config.interceptor;

import com.qq2008.common.entity.vo.MessageVO;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.data.GameServer;
import com.qq2008.game.bird.model.vo.GameStatusVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 游戏状态拦截器
 * 当游戏处于维护状态时禁止访问
 */
public class GameStatusHandlerInterceptor implements HandlerInterceptor {

    // 日志句柄
    private static final Logger logger = LoggerFactory.getLogger(GameStatusHandlerInterceptor.class);

    /***
     * R19 第二层防护。
     *
     * <p>为什么需要：{@code forward()} 会重新进入 DispatcherServlet 并再执行一遍拦截器链。
     * 正常情况下 {@code /message} 已被排除在拦截器之外，不会递归；但exclude 列表一旦被
     * 误改、漏改或路径写法不匹配（例如把 "/message" 误写成 "/message.do"），
     * 维护态下 /message 自身会再次被本拦截器拦截 → 再次 forward → 自我递归。
     * Spring 默认不限制嵌套深度，最终抛 StackOverflowError，且日志里只有一个栈看不出成因。
     *
     * <p>因此加一层与配置无关的兜底：本次请求已被转发过就放行。
     *
     * @param request request
     * @param response response
     * @param handler handler
     * @return true 放行 / false 终止
     * @throws Exception 转发异常
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 二次进入：本次请求已被 forward 到维护页，直接放行，不再重复 forward
        if (request.getAttribute(ConstData.MESSAGE_FORWARDED) != null) {
            return true;
        }
        // 方法拦截器且游戏不在运行中
        if (handler instanceof HandlerMethod && GameServer.getStatus() != GameStatusVO.RUNNING) {
            request.setAttribute("message", MessageVO.error("游戏正在维护中, 请稍后重新尝试访问。"));
            // 先打标记再转发：标记会随 request 一起进入新的 forward 目标
            request.setAttribute(ConstData.MESSAGE_FORWARDED, Boolean.TRUE);
            request.getRequestDispatcher(ConstData.MESSAGE_PATH).forward(request, response);
            return false;
        }
        return true;
    }
}
