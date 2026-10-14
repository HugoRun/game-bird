package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 小鸟数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("storage_bird")
@Getter
@Setter
public class StorageBird implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 小鸟唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 配置Id
     */
    private Integer birdId;

    /**
     * 等级
     */
    private Short level;

    /**
     * 经验
     */
    private Long exp;

    /**
     * 总经验
     */
    private Long totalExp;

    /**
     * 性别
     */
    private Byte sex;

    /**
     * 初始重量, 显示时/100
     */
    private Long initWeight;

    /**
     * 当前重量, 显示时/100
     */
    private Long weight;


    private Byte star;

    /**
     * 成长, 显示时/100
     */
    private Short grow;

    /**
     * 克制
     */
    private Short ko;

    /**
     * 是否锁定
     */
    private Byte isLock;

    /**
     * 捕捉时间戳
     */
    private Integer time;
}
