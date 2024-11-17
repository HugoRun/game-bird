package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 区域配置表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("base_field")
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

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
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
        return "BaseField{" +
            "id = " + id +
            ", name = " + name +
            ", descText = " + descText +
        "}";
    }
}
