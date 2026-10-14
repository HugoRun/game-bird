package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 小鸟配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_bird")
@Getter
@Setter
public class BaseBird implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 小鸟Id
     */
    private Integer id;

    /**
     * 等级
     */
    private Short lv;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String descText;

    /**
     * 类型:1敏捷,2力量,3魔法,4幻想
     */
    private Byte type;

    /**
     * 捕获场景Id
     */
    private Short field;

    /**
     * 最小重量
     */
    private Integer minWeight;

    /**
     * 最大重量
     */
    private Integer maxWeight;

    /**
     * 捕获获得经验
     */
    private Integer catchExp;

    /**
     * 捕获耗费分钟
     */
    private Short catchMin;

    /**
     * 出售每斤价格
     */
    private Integer price;

    /**
     * 状态
     */
    private Byte status;

    /**
     * s
     */
    private Integer s;

    /**
     * r
     */
    private Integer r;

    /**
     * lvu
     */
    private Integer lvu;

    /**
     * 权重
     */
    private Integer probability;
}
