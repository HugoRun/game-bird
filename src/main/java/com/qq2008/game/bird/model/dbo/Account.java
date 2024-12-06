package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 账号表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户Id
     */
    @TableId("user_id")
    private String userId;

    /**
     * 所属平台
     */
    private String platform;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户名
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 平台登录token
     */
    private String token;

    /**
     * 平台刷新token
     */
    private String refreshToken;

    /**
     * 平台用户Id
     */
    private String openId;

    /**
     * 创建时间戳
     */
    private Integer createTime;

    /**
     * 是否管理员
     */
    private Byte isAdmin;

    /**
     * 是否已封禁
     */
    private Byte isBan;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Byte getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Byte isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Byte getIsBan() {
        return isBan;
    }

    public void setIsBan(Byte isBan) {
        this.isBan = isBan;
    }

    @Override
    public String toString() {
        return "Account{" +
            "userId = " + userId +
            ", platform = " + platform +
            ", username = " + username +
            ", password = " + password +
            ", nickname = " + nickname +
            ", avatarUrl = " + avatarUrl +
            ", token = " + token +
            ", refreshToken = " + refreshToken +
            ", openId = " + openId +
            ", createTime = " + createTime +
            ", isAdmin = " + isAdmin +
            ", isBan = " + isBan +
        "}";
    }
}
