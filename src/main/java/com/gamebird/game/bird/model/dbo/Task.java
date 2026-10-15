package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 任务表
 * </p>
 */
@TableName("task")
@Getter
@Setter
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 任务类型:1每日登录/2捕捉小鸟/3购买道具
     */
    private Integer taskType;

    /**
     * 状态:0进行中/1已完成未领/2已领取
     */
    private Integer status;

    /**
     * 奖励金钱
     */
    private Long rewardCoin;

    /**
     * 创建时间戳
     */
    private Integer createTime;

    /**
     * 完成时间戳
     */
    private Integer finishTime;
}
