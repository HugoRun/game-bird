package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
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
        return "BaseRace{" +
            "id = " + id +
            ", name = " + name +
            ", descText = " + descText +
        "}";
    }
}
