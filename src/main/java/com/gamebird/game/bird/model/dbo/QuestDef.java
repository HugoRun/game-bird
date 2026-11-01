package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 奋进目标定义表（sql/extra/quest_tower.sql，新增表）
 * </p>
 */
@TableName("quest_def")
@Getter
@Setter
public class QuestDef implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 任务 id */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /** 任务名 */
    private String title;

    /** 达成条件说明 */
    private String descText;

    /** 1主线/2每日 */
    private Integer chapter;

    /** 进度事件:1登录/2捕捉/3购买/4训练/5孵化/6战斗胜/7出售/8等级/9图鉴 */
    private Integer eventType;

    /** 目标次数（等级型为目标等级） */
    private Integer targetNum;

    /** 前置任务id（0=无，主线串联） */
    private Integer preQuestId;

    /** 奖励金币 */
    private Long rewardCoin;

    /** 奖励钻石 */
    private Integer rewardDiamond;

    /** 奖励道具配置id（0=无） */
    private Integer rewardPropId;

    /** 奖励道具数量 */
    private Integer rewardPropNum;

    /** 排序 */
    private Integer sort;
}
