package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 公会表
 * </p>
 */
@TableName("guild")
@Getter
@Setter
public class Guild implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 公会名称
     */
    private String name;

    /**
     * 会长角色Id
     */
    private String leaderRoleId;

    /**
     * 公会等级
     */
    private Integer level;

    /**
     * 创建时间戳（秒）
     */
    private Integer createTime;
}
