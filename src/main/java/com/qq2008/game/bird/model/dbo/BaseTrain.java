package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 训练场配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_train")
@Getter
@Setter
public class BaseTrain implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 训练场Id
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
     * 最高训练等级
     */
    private Short maxLevel;

    /**
     * 每分钟获得经验值
     */
    private Integer minExp;

    /**
     * 功能描述
     */
    private String func;

    /**
     * 状态
     */
    private Byte status;

    /**
     * sort
     */
    private Integer sort;

    /**
     * lu
     */
    private Integer lu;

    /**
     * ut
     */
    private String ut;

    /**
     * ep
     */
    private Integer ep;

    /**
     * ad
     */
    private String ad;

    /**
     * 是否在商店出售
     */
    private Byte isShopping;
}
