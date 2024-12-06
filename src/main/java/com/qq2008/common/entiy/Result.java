package com.qq2008.common.entiy;


public class Result<T> {

    // 返回状态
    private Boolean flag;
    // 返回码
    private Integer code;
    // 返回信息
    private String message;
    // 返回数据
    private T data;

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

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

    public static <T> Result<T> ok() {
        return restResult(true, null, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> ok(T data) {
        return restResult(true, data, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> ok(T data, String message) {
        return restResult(true, data, ResultCode.SUCCESS.getCode(), message);
    }

    public static <T> Result<T> fail() {
        return restResult(false, null, ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage());
    }

    public static <T> Result<T> fail(ResultCode code) {
        return restResult(false, null, code.getCode(), code.getMessage());
    }

    public static <T> Result<T> fail(String message) {
        return restResult(false, message);
    }

    public static <T> Result<T> fail(T data) {
        return restResult(false, data, ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage());
    }

    public static <T> Result<T> fail(T data, String message) {
        return restResult(false, data, ResultCode.FAIL.getCode(), message);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return restResult(false, null, code, message);
    }

    private static <T> Result<T> restResult(Boolean flag, String message) {
        Result<T> res = new Result<>();
        res.setFlag(flag);
        res.setCode(flag ? ResultCode.SUCCESS.getCode() : ResultCode.FAIL.getCode());
        res.setMessage(message);
        return res;
    }

    private static <T> Result<T> restResult(Boolean flag, T data, Integer code, String message) {
        Result<T> res = new Result<>();
        res.setFlag(flag);
        res.setData(data);
        res.setCode(code);
        res.setMessage(message);
        return res;
    }

}
