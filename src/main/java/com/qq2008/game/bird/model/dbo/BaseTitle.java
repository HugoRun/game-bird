package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 称号配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-21
 */
@TableName("base_title")
@Getter
@Setter
public class BaseTitle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 称号Id
     */
    private Integer id;

    /**
     * 称号名称
     */
    private String name;

    /**
     * 称号描述
     */
    private String descText;
}
