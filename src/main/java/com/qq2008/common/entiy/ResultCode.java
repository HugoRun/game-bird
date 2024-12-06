package com.qq2008.common.entiy;

import java.util.List;

public enum ResultCode {

    // refresh_token仅一次有效，每次续票成功，都会返回最新的refresh_token
    // code是一次性有效的，开发者此时应该引导用户重新登录授权，重新获取code，而不是重试token接口
    // access token过期时间为60天
    // 如果存储的access token过期，请重新走登录流程

    // 命名规则
    // EMPTY        参数校验
    // NULL         数据不存在, 用于根据参数获取某值失败
    // INVALID      失效
    // CHECK        校验失败
    //

    // 调用次数限制
    //
    FAIL(0, "失败"),
    SUCCESS(200, "成功"),
    // client id
    CLIENT_ID_EMPTY(20001, "缺少参数client_id。"),
    CLIENT_ID_NULL(20002, "很抱歉，该网站尚未开通QQ帐号登录。"),
    // client secret
    CLIENT_SECRET_EMPTY(20011, "缺少参数client_secret。"),
    CLIENT_SECRET_NULL(20012, "client_secret非法。"),
    // authorization code
    AUTHORIZATION_CODE_EMPTY(20021, "缺少参数code。"),
    ERROR_AUTHORIZATION_CODE_INVALID(20022, "code失效"),
    AUTHORIZATION_CODE_NULL(20023, "获取code值失败。"),
    AUTHORIZATION_CODE_EXPIRE(20024, "code已过期。"),
    // access token
    ACCESS_TOKEN_EMPTY(20031, "缺少access token。"),
    ACCESS_TOKEN_INVALID(20032, "access token失效。"),
    ACCESS_TOKEN_NULL(20033, "access token验证失败。"),
    ACCESS_TOKEN_USER_NULL(20033, "access token获取用户失败。"),
    // refresh token
    REFRESH_TOKEN_EMPTY(20041, "缺少参数refresh token。"),
    REFRESH_TOKEN_INVALID(20042, "RefreshToken失效。"),
    REFRESH_TOKEN_NULL(20043, "获取refresh token值失败。"),
    // redirect url
    REDIRECT_URL_EMPTY(20051, "缺少参数redirect url。"),
    REDIRECT_URL_CHECK(20052, "回调地址redirect url不合法"),
    // request method
    REQUEST_METHOD_NOT_GET(20061, "HTTP请求非get方式。"),
    REQUEST_METHOD_NOT_POST(20062, "HTTP请求非post方式。"),
    // grant scopes
    CLIENT_GRANT_SCOPES_GET(20071, "获取app具有的权限列表失败。"),
    USER_CLIENT_GRANT_SCOPES_SET(20072, "设置用户对某app授权api列表失败。"),
    USER_CLIENT_NOT_GRANT(20073, "用户没有对该api进行授权"),
    // user baned
    USER_BANED(20081, "用户禁止登录"),
    IP_BANED(20082, "ip禁止登录"),
    // open id
    OPEN_ID_EMPTY(20091, "缺少参数openid。"),
    OPEN_ID_NULL(20092, "获取openid值失败。"),
    // redirect url
    AUTHORIZATION_SCOPE_EMPTY(20101, "缺少参数scope。"),
    AUTHORIZATION_SCOPE_CHECK(20102, "授权scope错误"),
    // open id
    SHARE_SITE_EMPTY(200201, "缺少参数share site。"),
    SHARE_SITE_CHECK(200202, "share site校验失败。"),
    SHARE_URL_EMPTY(200203, "缺少参数share url。"),
    SHARE_URL_CHECK(200204, "share url校验失败。"),
    SHARE_TITLE_EMPTY(200205, "缺少参数share title。"),
    SHARE_TITLE_CHECK(200206, "share title校验失败。"),
    //
    USERNAME_EMPTY(200211, "账号为空"),
    PASSWORD_EMPTY(200212, "密码为空"),
    PASSWORD2_EMPTY(200213, "重复密码为空"),
    USERNAME_OR_PASSWORD_CHECK(200214, "账号或密码错误"),
    VERIFICATION_CODE_EMPTY(200215, "验证码为空"),
    VERIFICATION_CODE_EXPIRE(200216, "验证码过期"),
    VERIFICATION_CODE_NULL(200217, "获取验证码失败"),
    VERIFICATION_CODE_USER_NULL(200218, "没有登录用户"),
    //
    SYSTEM_ERROR(9999999, "未知原因");

    // 错误码
    int code;
    // 错误消息
    String message;
    // 对象数据
    Object data;
    // 列表数据
    List<Object> dataList;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Object> getDataList() {
        return dataList;
    }

    public void setDataList(List<Object> dataList) {
        this.dataList = dataList;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
