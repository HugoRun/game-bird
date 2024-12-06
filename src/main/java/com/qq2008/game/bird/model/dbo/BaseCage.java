package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
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

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getParam() {
        return param;
    }

    public void setParam(Integer param) {
        this.param = param;
    }

    public Integer getLu() {
        return lu;
    }

    public void setLu(Integer lu) {
        this.lu = lu;
    }

    public Byte getIsShopping() {
        return isShopping;
    }

    public void setIsShopping(Byte isShopping) {
        this.isShopping = isShopping;
    }

    @Override
    public String toString() {
        return "BaseCage{" +
            "id = " + id +
            ", name = " + name +
            ", descText = " + descText +
            ", feeType = " + feeType +
            ", feePrice = " + feePrice +
            ", func = " + func +
            ", num = " + num +
            ", status = " + status +
            ", param = " + param +
            ", lu = " + lu +
            ", isShopping = " + isShopping +
        "}";
    }
}
