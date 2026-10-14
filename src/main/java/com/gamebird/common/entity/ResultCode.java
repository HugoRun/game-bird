package com.gamebird.common.entity;

/**
 * 统一业务返回码（游戏域）。
 *
 * <p><b>为什么重写</b>：原枚举 40 个值里，除 {@code FAIL(0)} / {@code SUCCESS(200)}
 * 外全部是 QQ 互联 OAuth 错误码（{@code CLIENT_ID_EMPTY}、{@code REFRESH_TOKEN_INVALID}、
 * {@code SHARE_URL_CHECK} …），来自作者的另一个项目；本项目 0 处业务引用。
 * 且原枚举中 {@code 20033} 被 {@code ACCESS_TOKEN_NULL} 与
 * {@code ACCESS_TOKEN_USER_NULL} 重复占用 —— 枚举值重复本身合法，但说明它从未被认真维护过。
 *
 * <p><b>文案红线</b>：{@link #ITEM_NOT_ON_SALE} 与 {@link #LEVEL_NOT_ENOUGH} 的默认文案
 * 与商店页逐字一致（「没有该商品哦。」「您的等级不足。」），改动会被购买流程的
 * 快照测试立即发现。需要带变量的文案（如「您的金钱不足哦(10/100)。」）请用
 * {@link BizException#BizException(ResultCode, String)} 传入自定义文案，
 * 不要改这里的默认值。
 */
public enum ResultCode {

    /** 成功。游戏域语义码，与 HTTP 状态码解耦（HTTP 侧仍返回 200）。 */
    SUCCESS(0, "成功"),
    /** 通用失败。 */
    FAIL(1000, "失败"),

    /** 商品不存在，或商品的支付类型不在已知的 3 种之内。 */
    ITEM_NOT_ON_SALE(1001, "没有该商品哦。"),
    /** 角色等级低于商品要求。 */
    LEVEL_NOT_ENOUGH(1002, "您的等级不足。"),
    /** 三种货币之一余额不足；具体文案由调用方拼装后传入。 */
    CURRENCY_NOT_ENOUGH(1003, "您的余额不足。"),
    /** 参数不合法：数量 ≤ 0、id ≤ 0、字符串超长等。 */
    PARAM_INVALID(1004, "参数不合法。"),
    /** 需要先登录账号。 */
    UNAUTHORIZED(1005, "请先登录。"),
    /** 需要先选择角色。 */
    ROLE_REQUIRED(1006, "请先选择角色。"),
    /** 游戏维护中。文案与 GameStatusHandlerInterceptor 现有文案逐字一致。 */
    MAINTENANCE(1007, "游戏正在维护中, 请稍后重新尝试访问。"),
    /** 未预期异常。 */
    SYSTEM_ERROR(9999, "系统繁忙，请稍后再试。");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}