package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 称号配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-21
 */
@TableName("base_title")
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

    @Override
    public String toString() {
        return "BaseTitle{" +
            "id = " + id +
            ", name = " + name +
            ", descText = " + descText +
        "}";
    }
}
