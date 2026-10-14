package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 区域配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_field")
@Getter
@Setter
public class BaseField implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景Id
     */
    private Short id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String descText;
}
