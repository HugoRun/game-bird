package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 每日签到表（sql/extra/sign_log.sql，新增表）
 * </p>
 */
@TableName("sign_log")
@Getter
@Setter
public class SignLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 唯一Id */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 所属角色Id */
    private String roleId;

    /** 签到日 yyyyMMdd（东八区） */
    private String signDate;

    /** 连续签到天数 */
    private Integer continuous;

    /** 本次奖励金币 */
    private Long rewardCoin;

    /** 本次奖励钻石 */
    private Integer rewardDiamond;

    /** 创建时间戳 */
    private Integer createTime;
}
