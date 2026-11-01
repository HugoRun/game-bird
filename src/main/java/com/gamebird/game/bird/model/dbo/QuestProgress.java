package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 奋进目标进度表（sql/extra/quest_tower.sql，新增表）
 * </p>
 */
@TableName("quest_progress")
@Getter
@Setter
public class QuestProgress implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 所属角色Id */
    private String roleId;

    /** 任务id */
    private Integer questId;

    /** 当前进度 */
    private Integer progress;

    /** 0进行中/1可交付/2已交付 */
    private Integer status;

    /** 每日任务的归属日 yyyyMMdd */
    private String dayDate;

    /** 创建时间戳 */
    private Integer createTime;

    /** 完成时间戳 */
    private Integer finishTime;
}
