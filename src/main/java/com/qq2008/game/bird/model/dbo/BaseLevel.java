package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 角色等级配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_level")
@Getter
@Setter
public class BaseLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 等级
     */
    @TableId("level")
    private Short level;

    /**
     * 升到本级需要经验
     */
    private Integer costExp;

    /**
     * 升到本级累计需要经验
     */
    private Long totalExp;
}
