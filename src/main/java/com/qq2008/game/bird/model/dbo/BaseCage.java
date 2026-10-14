package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 笼子配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_cage")
@Getter
@Setter
public class BaseCage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笼子Id
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
     * 功能描述
     */
    private String func;

    /**
     * 耐久度
     */
    private Integer num;

    /**
     * 状态
     */
    private Byte status;

    /**
     * param
     */
    private Integer param;

    /**
     * lu
     */
    private Integer lu;

    /**
     * 是否在商店出售
     */
    private Byte isShopping;
}
