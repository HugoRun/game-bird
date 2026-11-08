package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 陷阱配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_trap")
@Getter
@Setter
public class BaseTrap implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 陷阱Id
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
     * 价格类型,1金币/2元宝/3爱心
     */
    private Byte feeType;

    /**
     * 价格数值
     */
    private Integer feePrice;

    /**
     * 效果值
     */
    private Integer effectValue;

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
     * lu
     */
    private Integer lu;

    /**
     * ad
     */
    private String ad;

    private Integer lb;

    /**
     * 是否在商店出售
     */
    private Byte isShopping;
}
