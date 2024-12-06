package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * VIP配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_vip")
public class BaseVip implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * vip等级
     */
    @TableId("level")
    private Integer level;

    /**
     * 充值金额
     */
    private Integer pay;

    /**
     * 解锁功能描述
     */
    private String info;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getPay() {
        return pay;
    }

    public void setPay(Integer pay) {
        this.pay = pay;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "BaseVip{" +
            "level = " + level +
            ", pay = " + pay +
            ", info = " + info +
        "}";
    }
}
