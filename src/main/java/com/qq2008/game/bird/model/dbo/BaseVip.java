package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * VIP配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_vip")
@Getter
@Setter
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
}
