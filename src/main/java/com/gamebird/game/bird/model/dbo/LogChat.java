package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 聊天日志记录表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("log_chat")
@Getter
@Setter
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
}
