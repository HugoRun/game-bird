package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 诱饵配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_bait")
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getBirdSet() {
        return birdSet;
    }

    public void setBirdSet(String birdSet) {
        this.birdSet = birdSet;
    }

    public Byte getFeeType() {
        return feeType;
    }

    public void setFeeType(Byte feeType) {
        this.feeType = feeType;
    }

    public Integer getFeePrice() {
        return feePrice;
    }

    public void setFeePrice(Integer feePrice) {
        this.feePrice = feePrice;
    }

    public Integer getExp() {
        return exp;
    }

    public void setExp(Integer exp) {
        this.exp = exp;
    }

    public Byte getSex() {
        return sex;
    }

    public void setSex(Byte sex) {
        this.sex = sex;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Short getLu() {
        return lu;
    }

    public void setLu(Short lu) {
        this.lu = lu;
    }

    public Byte getLb() {
        return lb;
    }

    public void setLb(Byte lb) {
        this.lb = lb;
    }

    public Integer getUt() {
        return ut;
    }

    public void setUt(Integer ut) {
        this.ut = ut;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Byte getIsShopping() {
        return isShopping;
    }

    public void setIsShopping(Byte isShopping) {
        this.isShopping = isShopping;
    }

    @Override
    public String toString() {
        return "BaseBait{" +
            "id = " + id +
            ", name = " + name +
            ", descText = " + descText +
            ", birdSet = " + birdSet +
            ", feeType = " + feeType +
            ", feePrice = " + feePrice +
            ", exp = " + exp +
            ", sex = " + sex +
            ", status = " + status +
            ", lu = " + lu +
            ", lb = " + lb +
            ", ut = " + ut +
            ", num = " + num +
            ", isShopping = " + isShopping +
        "}";
    }
}
