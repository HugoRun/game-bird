package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 收藏品数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("storage_collect")
@Getter
@Setter
public class StorageCollect implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 小鸟唯一Id
     */
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
     * 性别
     */
    private Byte sex;

    /**
     * 初始重量
     */
    private Long initWeight;

    /**
     * 当前重量
     */
    private Long weight;

    /**
     * 成长
     */
    private Short grow;

    /**
     * 克制
     */
    private Short restrained;

    /**
     * 是否锁定
     */
    private Byte isLock;

    /**
     * 捕捉时间戳
     */
    private Integer time;
}
