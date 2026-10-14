package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 诱饵配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_bait")
@Getter
@Setter
public class BaseBait implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 诱饵Id
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
     * 可捕捉小鸟列表, 小鸟Id,...
     */
    private String birdSet;

    /**
     * 价格类型,1金币/2元宝/3爱心
     */
    private Byte feeType;

    /**
     * 价格数值
     */
    private Integer feePrice;

    /**
     * 经验
     */
    private Integer exp;

    /**
     * 性别
     */
    private Byte sex;

    /**
     * 状态
     */
    private Byte status;

    /**
     * 购买等级限制
     */
    private Short lu;

    /**
     * 使用区域限制
     */
    private Byte lb;

    /**
     * ut
     */
    private Integer ut;

    /**
     * 单次购买数量
     */
    private Integer num;

    /**
     * 是否在商店出售
     */
    private Byte isShopping;
}
