package com.qq2008.game.bird.model.vo;

/***
 * 游戏状态
 */
public enum GameStatusVO {

    // 状态
    NULL(0, "停机中"),
    MAINTENANCE(1, "维护中..."),
    LOADING(2, "启动中..."),
    RUNNING(3, "运行中...");

    // 编码
    int code;
    // 说明
    String message;

    GameStatusVO(int code, String message) {
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
}
