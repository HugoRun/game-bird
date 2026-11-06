package com.gamebird.common.entity;

/**
 * 业务异常：携带错误码与<b>明确的消息</b>。
 *
 * <p><b>为什么取代 {@code throw new Exception()}</b>：改造前
 * {@code BaseController} 的 9 个数值方法都是同一个模板 ——
 * 参数非法时 {@code throw new Exception()}（<b>无消息</b>），外层 catch 后
 * {@code throw new RuntimeException(e)}。两层异常都没有消息，日志里只剩一句
 * {@code java.lang.Exception}，完全无法判断是「参数非法」还是「余额不足」。
 *
 * <p><b>继承 RuntimeException 的原因</b>：让既有调用点不必改签名即可继续工作
 * （方法签名里没有受检异常）。注意捕获方若原先写的是 {@code catch (RuntimeException e)}，
 * 仍然能捕获本异常，行为兼容。
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ResultCode code;

    /**
     * 用错误码自带的默认文案构造。
     *
     * @param code 错误码；不可为 null
     */
    public BizException(ResultCode code) {
        super(code.getMessage());
        this.code = code;
    }

    /**
     * 用自定义文案构造。错误码仍决定 JSON 响应侧的判定，因此带变量的文案
     * （如「您的金钱不足哦(10/100)。」）必须走这个构造器，而不是改
     * {@link ResultCode} 的默认值。
     *
     * @param code    错误码；不可为 null
     * @param message 展示给用户的文案
     */
    public BizException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    public ResultCode getCode() {
        return code;
    }

    /**
     * 便利工厂：支付类型非法时复用「没有该商品哦。」文案，
     * 保证与重构前的用户可见行为逐字一致。
     *
     * @return 商品不可售异常
     */
    public static BizException itemNotOnSale() {
        return new BizException(ResultCode.ITEM_NOT_ON_SALE);
    }
}