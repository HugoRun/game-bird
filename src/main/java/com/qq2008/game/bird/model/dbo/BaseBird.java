package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 小鸟配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_bird")
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Short getLv() {
        return lv;
    }

    public void setLv(Short lv) {
        this.lv = lv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescText() {
        return descText;
    }

    public void setDescText(String descText) {
        this.descText = descText;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Short getField() {
        return field;
    }

    public void setField(Short field) {
        this.field = field;
    }

    public Integer getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(Integer minWeight) {
        this.minWeight = minWeight;
    }

    public Integer getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(Integer maxWeight) {
        this.maxWeight = maxWeight;
    }

    public Integer getCatchExp() {
        return catchExp;
    }

    public void setCatchExp(Integer catchExp) {
        this.catchExp = catchExp;
    }

    public Short getCatchMin() {
        return catchMin;
    }

    public void setCatchMin(Short catchMin) {
        this.catchMin = catchMin;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getS() {
        return s;
    }

    public void setS(Integer s) {
        this.s = s;
    }

    public Integer getR() {
        return r;
    }

    public void setR(Integer r) {
        this.r = r;
    }

    public Integer getLvu() {
        return lvu;
    }

    public void setLvu(Integer lvu) {
        this.lvu = lvu;
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        this.probability = probability;
    }

    @Override
    public String toString() {
        return "BaseBird{" +
            "id = " + id +
            ", lv = " + lv +
            ", name = " + name +
            ", descText = " + descText +
            ", type = " + type +
            ", field = " + field +
            ", minWeight = " + minWeight +
            ", maxWeight = " + maxWeight +
            ", catchExp = " + catchExp +
            ", catchMin = " + catchMin +
            ", price = " + price +
            ", status = " + status +
            ", s = " + s +
            ", r = " + r +
            ", lvu = " + lvu +
            ", probability = " + probability +
        "}";
    }
}
