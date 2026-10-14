package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_race")
@Getter
@Setter
public class BaseRace implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 基因类型
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
}
