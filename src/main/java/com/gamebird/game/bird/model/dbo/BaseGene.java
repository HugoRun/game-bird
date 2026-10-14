package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 基因配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_gene")
@Getter
@Setter
public class BaseGene implements Serializable {

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
