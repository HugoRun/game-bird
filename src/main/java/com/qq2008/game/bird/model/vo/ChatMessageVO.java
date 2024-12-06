package com.qq2008.game.bird.model.vo;

/***
 * 消息VO
 */
public class ChatMessageVO {
    // 角色信息
    private ChatRoleVO role;
    // 消息内容
    private String message;
    // 发送时间戳
    private int time;

    public ChatRoleVO getRole() {
        return role;
    }

    public void setRole(ChatRoleVO role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
