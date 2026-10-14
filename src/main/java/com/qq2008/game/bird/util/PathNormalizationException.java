package com.qq2008.game.bird.util;

import java.io.Serial;

/**
 * 路径规范化失败时抛出的类型化异常。
 *
 * <p><b>为什么不直接抛 {@link IllegalArgumentException}</b>：调用方需要区分
 * 「输入为空」「越界」「撞上Windows 保留名」这几类失败——它们的处置方式完全不同：
 * 空输入可以退化成默认值，越界必须拒绝（安全），保留名只能改名。
 * 一种异常类型只能让调用方靠读message 文案来分支，那是脆弱的。
 * 故本类携带机器可读的 {@link Reason}。
 *
 * <p><b>继承 {@link IllegalArgumentException} 的取舍</b>：继承它可以让现有
 * {@code catch (IllegalArgumentException)} 的调用方不被新异常穿透，代价是
 * 本异常也会被过宽的catch 捕获。考虑到本类已有类型名与 {@link Reason} 做二级分��，
 * 且「参数不合法」的语义确实成立，此处接受这一取舍。
 *
 * <p>本异常是<b>受检异常</b>，强制调用方显式决定「越界时怎么办」——
 * 这正是安全相关失败路径应有的样子。
 */
public class PathNormalizationException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 失败原因枚举。
     *
     * <p>每项带中文说明，便于日志与告警直接可读——失败原因往往要出现在运维工单里。
     */
    public enum Reason {

        /** 输入为 {@code null}。 */
        NULL_INPUT("输入为 null"),

        /** 输入为空字符串。 */
        EMPTY_INPUT("输入为空字符串"),

        /** 输入只有空白字符。 */
        BLANK_INPUT("输入只有空白字符"),

        /** 规范化后没有任何有效片段，例如 {@code "."} 或 {@code "a/.."}。 */
        NO_SEGMENTS("规范化后没有任何有效片段"),

        /** 含 Windows 保留设备名（{@code CON}、{@code NUL} 等），该名称在 Windows 上无法创建。 */
        RESERVED_NAME("含 Windows 保留设备名"),

        /** 相对路径中的 {@code ..} 越出根目录，例如 {@code a/../../b}。 */
        ESCAPES_ROOT("相对路径中的 .. 越出根目录"),

        /** 单段超过 255 字符，或总长超过 4096 字符。 */
        TOO_LONG("路径或其中某段超过长度上限"),

        /** 路径形态非法，例如盘符位置出现多个冒号、UNC 前缀不完整。 */
        NOT_A_PATH("路径形态非法");

        private final String description;

        Reason(String description) {
            this.description = description;
        }

        /**
         * 取中文说明。
         *
         * @return 不可变的中文描述
         */
        public String description() {
            return description;
        }
    }

    /** 触发失败的原始输入；为 {@code null} 时原样保留。 */
    private final String rawInput;

    /** 机器可读的失败原因。 */
    private final Reason reason;

    /**
     * 构造异常。
     *
     * @param rawInput 触发失败的原始输入；可为 {@code null}
     * @param reason   失败原因；不可为 {@code null}
     */
    public PathNormalizationException(String rawInput, Reason reason) {
        super(buildMessage(rawInput, reason));
        this.rawInput = rawInput;
        this.reason = reason;
    }

    /**
     * 取触发失败的原始输入。
     *
     * @return 原始输入；调用方传入的即为 {@code null}
     */
    public String getRawInput() {
        return rawInput;
    }

    /**
     * 取失败原因。
     *
     * @return 失败原因枚举，永不为 {@code null}
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * 拼装可读消息：同时带上原始输入与原因，避免调用方只拿到一句无上下文的描述。
     *
     * @param rawInput 原始输入
     * @param reason   失败原因
     * @return 完整消息
     */
    private static String buildMessage(String rawInput, Reason reason) {
        StringBuilder sb = new StringBuilder("路径规范化失败[");
        sb.append(reason == null ? "UNKNOWN" : reason.name());
        sb.append("]：").append(reason == null ? "未知原因" : reason.description());
        sb.append("；原始输入=<");
        sb.append(rawInput == null ? "null" : rawInput);
        sb.append('>');
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     *
     * <p>覆写为「原因 + 原始输入」的固定格式，便于日志聚合时按前缀分组。
     *
     * @return 结构化字符串
     */
    @Override
    public String toString() {
        return "PathNormalizationException{reason=" + reason
                + ", rawInput=<" + (rawInput == null ? "null" : rawInput) + ">}";
    }
}
