package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 道具配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_prop")
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

    public String getAwardList() {
        return awardList;
    }

    public void setAwardList(String awardList) {
        this.awardList = awardList;
    }

    public Short getEffectType() {
        return effectType;
    }

    public void setEffectType(Short effectType) {
        this.effectType = effectType;
    }

    public Integer getEffectValue() {
        return effectValue;
    }

    public void setEffectValue(Integer effectValue) {
        this.effectValue = effectValue;
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

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getUt() {
        return ut;
    }

    public void setUt(Integer ut) {
        this.ut = ut;
    }

    public Integer getLu() {
        return lu;
    }

    public void setLu(Integer lu) {
        this.lu = lu;
    }

    public Integer getEp() {
        return ep;
    }

    public void setEp(Integer ep) {
        this.ep = ep;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Byte getIsShopping() {
        return isShopping;
    }

    public void setIsShopping(Byte isShopping) {
        this.isShopping = isShopping;
    }

    @Override
    public String toString() {
        return "BaseProp{" +
            "id = " + id +
            ", name = " + name +
            ", descText = " + descText +
            ", awardList = " + awardList +
            ", effectType = " + effectType +
            ", effectValue = " + effectValue +
            ", feeType = " + feeType +
            ", feePrice = " + feePrice +
            ", num = " + num +
            ", func = " + func +
            ", status = " + status +
            ", ut = " + ut +
            ", lu = " + lu +
            ", ep = " + ep +
            ", ad = " + ad +
            ", sort = " + sort +
            ", isShopping = " + isShopping +
        "}";
    }
}
