package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 好友表
 * </p>
 */
@TableName("friend")
@Getter
@Setter
public class Friend implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 好友角色Id
     */
    private String friendRoleId;

    /**
     * 好友名称
     */
    private String friendName;

    /**
     * 添加时间戳（秒）
     */
    private Integer createTime;
}
