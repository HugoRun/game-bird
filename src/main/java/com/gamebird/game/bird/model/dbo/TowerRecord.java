package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 试炼之塔记录表（sql/extra/quest_tower.sql，新增表）
 * </p>
 */
@TableName("tower_record")
@Getter
@Setter
public class TowerRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 所属角色Id */
    private String roleId;

    /** 当前挑战层 */
    private Integer floor;

    /** 历史最高层 */
    private Integer bestFloor;

    /** 当日挑战计数归属日 yyyyMMdd */
    private String dailyDate;

    /** 当日已挑战次数 */
    private Integer dailyAttempts;

    /** 更新时间戳 */
    private Integer updateTime;
}
