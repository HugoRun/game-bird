package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 训练场配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_train")
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

    public Short getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(Short maxLevel) {
        this.maxLevel = maxLevel;
    }

    public Integer getMinExp() {
        return minExp;
    }

    public void setMinExp(Integer minExp) {
        this.minExp = minExp;
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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getLu() {
        return lu;
    }

    public void setLu(Integer lu) {
        this.lu = lu;
    }

    public String getUt() {
        return ut;
    }

    public void setUt(String ut) {
        this.ut = ut;
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

    public Byte getIsShopping() {
        return isShopping;
    }

    public void setIsShopping(Byte isShopping) {
        this.isShopping = isShopping;
    }

    @Override
    public String toString() {
        return "BaseTrain{" +
            "id = " + id +
            ", name = " + name +
            ", descText = " + descText +
            ", feeType = " + feeType +
            ", feePrice = " + feePrice +
            ", maxLevel = " + maxLevel +
            ", minExp = " + minExp +
            ", func = " + func +
            ", status = " + status +
            ", sort = " + sort +
            ", lu = " + lu +
            ", ut = " + ut +
            ", ep = " + ep +
            ", ad = " + ad +
            ", isShopping = " + isShopping +
        "}";
    }
}
