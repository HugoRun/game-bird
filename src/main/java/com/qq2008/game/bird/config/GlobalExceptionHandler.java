package com.qq2008.game.bird.config;

import com.qq2008.common.entity.BizException;
import com.qq2008.common.entity.Result;
import com.qq2008.common.entity.ResultCode;
import com.qq2008.common.entity.vo.MessageVO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理。
 *
 * <p><b>为什么需要它</b>：改造前所有业务失败都以 {@code throw new Exception("...")} 的形式
 * 抛出且不带消息，最终由容器返回 Whitelabel Error Page，玩家看到的是英文堆栈页；
 * 而 {@code @Controller} 内又普遍 {@code catch (Exception e)} 只打日志、返回裸视图名，
 * 两套失败语义并存。本类把失败收敛成两条明确出口：
 *
 * <ul>
 *   <li><b>HTML 页面请求</b> → 渲染 {@code error.html}，模型里带 {@code message}（{@link MessageVO}），
 *       与项目既有的「球球提示」风格一致，复用 {@code fragment/message} 片段；</li>
 *   <li><b>JSON / AJAX 请求</b> → 返回 {@link Result} JSON，结构与业务失败返回完全一致，
 *       前端无需区分「异常」与「业务失败」两种报文。</li>
 * </ul>
 *
 * <p><b>协商规则</b>：优先看 {@code X-Requested-With: XMLHttpRequest}（项目内 AJAX 全部带此头），
 * 其次看 Accept 是否显式要求 JSON 且不含 HTML，否则按页面请求处理。
 * 路由已统一去掉历史后缀，故不再按路径后缀做判断。
 *
 * <p><b>注意</b>：本类只负责「未被业务代码捕获的异常」。已被 {@code catch} 吞掉并
 * {@code return "xxx"} 的分支不在此列——那是文档 R19（ResultDispatcher 重入）要解决的另一问题。
 *
 * @see Result 统一返回体
 * @see ResultCode 业务返回码
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 静态资源路径前缀，命中则不套用游戏皮肤（避免把 CSS 404 渲染成一整页 HTML）。 */
    private static final String ASSETS_PREFIX = "/assets/";

    /** 异常页视图名，对应 {@code templates/error.html}。 */
    private static final String ERROR_VIEW = "error";

    /**
     * 业务异常：已带明确错误码与文案，属于可预期失败。
     *
     * <p>业务失败用 200 响应 + 业务码，避免前端把「余额不足」当成系统故障弹错。
     *
     * @param e       业务异常
     * @param request 当前请求
     * @return JSON 或 HTML
     */
    @ExceptionHandler(BizException.class)
    public Object handleBizException(BizException e, HttpServletRequest request) {
        logger.warn("业务异常 code={} message={} uri={}",
                e.getCode().getCode(), e.getMessage(), request.getRequestURI());
        return respond(request, e.getCode(), e.getMessage(), HttpStatus.OK);
    }

    /**
     * 404：请求路径没有对应处理器。
     *
     * <p><b>为什么必须显式声明</b>：下面 {@code handleException(Exception)} 会把
     * {@code NoHandlerFoundException} 一并吞掉，导致任何不存在的路径都返回
     * 500「系统繁忙」—— 运维与前端都无法区分「地址写错」和「服务坏了」。
     * Spring 的选择器按「最具体优先」匹配，本方法因此会先于兜底方法命中。
     *
     * @param e       未命中处理器的异常
     * @param request 当前请求
     * @return JSON或 HTML
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFound(NoHandlerFoundException e, HttpServletRequest request) {
        logger.warn("请求的路径不存在 uri={}", request.getRequestURI());
        return respond(request, ResultCode.SYSTEM_ERROR, "页面不存在。", HttpStatus.NOT_FOUND);
    }

    /**
     * 404：路径没匹配到任何 Controller，Spring 转而去找静态资源也没找到。
     *
     * <p><b>与上面 {@code NoHandlerFoundException} 的区别</b>：后者只在
     * {@code DispatcherServlet.throwExceptionIfNoHandlerFound} 为 true 时抛出
     * （本项目未开启）；Spring Boot 3默认开启静态资源处理，未匹配的路径会落到
     * {@code ResourceHttpRequestHandler}，由它抛 {@code NoResourceFoundException}。
     * 因此<b>只处理 NoHandlerFoundException 是无效的</b>—— 兜底的
     * {@code handleException(Exception)} 会把它吞成 500。
     *
     * <p><b>为什么本轮才暴露</b>：全项目去掉 {@code .do} 后缀后，旧链接
     * （如 {@code /game.do}）全部变成「无Controller 映射」的路径。若不做处理，
     * 每一个失效书签都会看到 500「系统繁忙」，运维会误判为服务故障。
     *
     * @param e       静态资源不存在异常
     * @param request 当前请求
     * @return JSON 或 HTML
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        logger.warn("请求的路径不存在 uri={}", request.getRequestURI());
        return respond(request, ResultCode.SYSTEM_ERROR, "页面不存在。", HttpStatus.NOT_FOUND);
    }

    /**
     * 405：请求方法不被该路径支持。
     *
     * <p><b>为什么必须显式声明</b>：下面 {@code handleException(Exception)} 会把
     * {@code HttpRequestMethodNotSupportedException} 一并吞掉。典型场景是「把只有
     * {@code @PostMapping} 的路由用 GET 访问」—— 例如误把 {@code /login} 的 POST 当GET 用。
     * 改造前这会返回 500「系统繁忙」，运维与前端都会误判为服务故障，而真实原因只是方法不对。
     *
     * <p>响应体带 {@code Allow} 头，告知客户端该路径支持哪些方法。
     *
     * <p>★ 注意：状态码必须是 {@link HttpStatus#METHOD_NOT_ALLOWED}。若误用
     * {@code HttpStatus.OK}，405 会被响应成 200，比原来的 500 更糟（客户端会认为成功）。
     *
     * @param e       方法不允许异常
     * @param request 当前请求
     * @return JSON 或 HTML
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String allow = String.join(", ", e.getSupportedMethods() == null
                ? new String[]{"?"} : e.getSupportedMethods());
        logger.warn("请求方法不被支持 uri={} method={} allow={}",
                request.getRequestURI(), request.getMethod(), allow);
        return respond(request, ResultCode.PARAM_INVALID,
                "请求方式不正确，请用 " + allow + " 方式访问该地址。", HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 兜底：所有未被上面捕获的异常。
     *
     * @param e       异常
     * @param request 当前请求
     * @return JSON 或 HTML
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        logger.error("未捕获异常 uri={}", request.getRequestURI(), e);
        return respond(request, ResultCode.SYSTEM_ERROR,
                ResultCode.SYSTEM_ERROR.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 按请求类型选择出口。
     *
     * @param request 当前请求
     * @param code    业务返回码
     * @param message 提示文案
     * @param status  HTTP 状态
     * @return JSON 响应体或已渲染的 ModelAndView
     */
    private Object respond(HttpServletRequest request, ResultCode code, String message, HttpStatus status) {
        if (isJsonRequest(request)) {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Result.fail(code, message));
        }
        ModelAndView view = new ModelAndView(ERROR_VIEW, status);
        view.addObject("message", MessageVO.error(message));
        view.addObject("code", code.getCode());
        return view;
    }

    /**
     * 判断是否为JSON / AJAX 请求。
     *
     * @param request 当前请求
     * @return true 表示应返回 JSON
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }
        String uri = request.getRequestURI();
        // 静态资源一律不协商 JSON，让容器原生 404 处理
        if (uri != null && uri.startsWith(ASSETS_PREFIX)) {
            return false;
        }
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)
                && !accept.contains(MediaType.TEXT_HTML_VALUE);
    }
}