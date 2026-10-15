package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 战斗记录表
 * </p>
 *
 * @author game-bird
 * @since 2026-10-14
 */
@TableName("fight_log")
@Getter
@Setter
public class FightLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 挑战者角色Id
     */
    private String roleId;

    /**
     * 对手名称
     */
    private String opponentName;

    /**
     * 结果:0负/1胜
     */
    private Integer result;

    /**
     * 胜利奖励金钱
     */
    private Long rewardCoin;

    /**
     * 战斗时间戳（秒级）
     */
    private Integer fightTime;
}
