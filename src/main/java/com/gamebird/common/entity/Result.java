package com.gamebird.common.entity;

/**
 * 统一的 AJAX / JSON 返回体。
 *
 * <p><b>为什么瘦身</b>：原类有 11 个静态工厂、2 个私有 {@code restResult}，其中存在
 * <b>类型擦除冲突</b>——{@code fail(String)} 与 {@code fail(T)} 在编译期同为
 * {@code fail(Object)}，调用 {@code fail("文案")} 时编译器只能选更具体的 {@code fail(String)}，
 * 于是「带 data 的失败」永远无法用一行字符串消息表达，静默丢 data。同理
 * {@code ok(T)} 与 {@code ok(T, String)} 也让调用者必须记忆参数顺序。
 *
 * <p>此外 {@code flag} 字段与 {@code code} 完全同源（{@code flag == (code == SUCCESS)}），
 * 属于冗余的第二真理来源，序列化后前端还要判断以哪个为准。这里删除 {@code flag}，
 * <b>判定成功请看 {@link ResultCode#SUCCESS} 的 code</b>。
 *
 * <p><b>保留的工厂</b>只有语义无歧义的 6 个：
 * <ul>
 *   <li>{@code ok()} / {@code ok(T data)} / {@code ok(T data, String message)}</li>
 *   <li>{@code fail()} / {@code fail(ResultCode code)} / {@code fail(ResultCode code, String message)}</li>
 * </ul>
 * 失败一律以 {@link ResultCode} 表达错误类别，自定义文案走 {@code fail(code, message)}；
 * 不再提供 {@code fail(T data)} 这种「用重载区分成败」的写法。
 *
 * @param <T> 业务数据类型
 */
public class Result<T> {

    /** 业务返回码，取值见 {@link ResultCode}；与 HTTP 状态码解耦。 */
    private Integer code;

    /** 提示文案。 */
    private String message;

    /** 业务数据，失败时通常为 {@code null}。 */
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /** 成功，无数据。 */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /** 成功，携带数据。 */
    public static <T> Result<T> ok(T data) {
        return of(ResultCode.SUCCESS, ResultCode.SUCCESS.getMessage(), data);
    }

    /** 成功，携带数据并覆盖提示文案。 */
    public static <T> Result<T> ok(T data, String message) {
        return of(ResultCode.SUCCESS, message, data);
    }

    /** 失败，使用 {@link ResultCode#FAIL} 的默认文案。 */
    public static <T> Result<T> fail() {
        return fail(ResultCode.FAIL);
    }

    /** 失败，使用错误码自带的默认文案。 */
    public static <T> Result<T> fail(ResultCode code) {
        return of(code, code.getMessage(), null);
    }

    /** 失败，覆盖文案（例如拼装「您的金钱不足哦(10/100)」这类带变量的提示）。 */
    public static <T> Result<T> fail(ResultCode code, String message) {
        return of(code, message, null);
    }

    private static <T> Result<T> of(ResultCode code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}