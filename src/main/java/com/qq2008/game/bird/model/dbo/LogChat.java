package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 聊天日志记录表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("log_chat")
public class LogChat implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送玩家Id
     */
    private String roleId;

    /**
     * 发送玩家名称
     */
    private String roleName;

    /**
     * 发送玩家称号名称
     */
    private String titleName;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 发送时间戳
     */
    private Integer time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "LogChat{" +
            "id = " + id +
            ", roleId = " + roleId +
            ", roleName = " + roleName +
            ", titleName = " + titleName +
            ", message = " + message +
            ", time = " + time +
        "}";
    }
}
