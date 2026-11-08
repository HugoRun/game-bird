package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 道具配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_prop")
@Getter
@Setter
public class BaseProp implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 道具Id
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String descText;

    /**
     * 礼包奖励列表
     */
    private String awardList;

    /**
     * 效果类型
     */
    private Short effectType;

    /**
     * 效果值
     */
    private Integer effectValue;

    /**
     * 价格类型,1金币/2元宝/3爱心
     */
    private Byte feeType;

    /**
     * 价格数值
     */
    private Integer feePrice;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 功能描述
     */
    private String func;

    /**
     * 状态
     */
    private Byte status;

    /**
     * ut
     */
    private Integer ut;

    /**
     * lu
     */
    private Integer lu;

    /**
     * ep
     */
    private Integer ep;

    /**
     * ad
     */
    private String ad;

    /**
     * sort
     */
    private Integer sort;

    /**
     * 是否在商店出售
     */
    private Byte isShopping;
}
