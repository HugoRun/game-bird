package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 账号表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("account")
@Getter
@Setter
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
    /**
     * 刻意不使用 Lombok 的 {@code @Data}：它生成的 toString 会把 password 字段
     * （本项目是明文存储）的值一起打印出来，一旦有代码记录该对象或抛异常带出上下文，
     * 密码就会进日志。
     *
     * <p>改造前本类手写的 toString 正是这么做的，属于既有的敏感信息泄漏，
     * 此处一并修复：只输出非敏感字段，密码与两个 token 一律不输出。
     *
     * @return 不含密码与token 的对象描述
     */
    @Override
    public String toString() {
        return "Account{" +
            "userId = " + userId +
            ", platform = " + platform +
            ", username = " + username +
            ", nickname = " + nickname +
            ", avatarUrl = " + avatarUrl +
            ", createTime = " + createTime +
            ", isAdmin = " + isAdmin +
            ", isBan = " + isBan +
        "}";
    }
}
