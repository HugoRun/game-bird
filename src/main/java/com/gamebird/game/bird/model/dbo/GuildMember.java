package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 公会成员表
 * </p>
 */
@TableName("guild_member")
@Getter
@Setter
public class GuildMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 公会Id
     */
    private Integer guildId;

    /**
     * 成员角色Id
     */
    private String roleId;

    /**
     * 加入时间戳（秒）
     */
    private Integer joinTime;
}
