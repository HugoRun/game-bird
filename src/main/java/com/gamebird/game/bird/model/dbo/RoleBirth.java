package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 孵化场数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_birth")
@Getter
@Setter
public class RoleBirth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 孵化场唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 排序Id
     */
    private Integer idx;

    /**
     * 小鸟Id
     */
    private Integer birdId;

    /**
     * 小鸟配置Id
     */
    private Integer birdBaseId;

    /**
     * 对方玩家Id
     */
    private Integer matchRoleId;

    /**
     * 对方玩家名称
     */
    private String matchRoleName;

    /**
     * 对方小鸟唯一Id
     */
    private Integer matchBirdId;

    /**
     * 对方小鸟配置Id
     */
    private Integer matchBirdBaseId;

    /**
     * 开始配对时间戳
     */
    private Integer birthTime;

    /**
     * 配置结束时间戳
     */
    private Integer birthEndTime;

    /**
     * 使用巢穴Id
     */
    private Integer useNestId;

    /**
     * 使用巢穴剩余次数
     */
    private Integer useNestNum;
}
