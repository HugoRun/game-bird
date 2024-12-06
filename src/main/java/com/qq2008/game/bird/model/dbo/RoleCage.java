package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 鸟笼数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_cage")
public class RoleCage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 鸟笼唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 小鸟唯一Id
     */
    private Integer idx;

    /**
     * 排序Id
     */
    private Integer birdId;

    /**
     * 小鸟配置Id
     */
    private Integer baseBridId;

    /**
     * 使用鸟笼Id
     */
    private Integer useCageId;

    /**
     * 使用鸟笼剩余次数
     */
    private Integer useCageNum0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public Integer getBirdId() {
        return birdId;
    }

    public void setBirdId(Integer birdId) {
        this.birdId = birdId;
    }

    public Integer getBaseBridId() {
        return baseBridId;
    }

    public void setBaseBridId(Integer baseBridId) {
        this.baseBridId = baseBridId;
    }

    public Integer getUseCageId() {
        return useCageId;
    }

    public void setUseCageId(Integer useCageId) {
        this.useCageId = useCageId;
    }

    public Integer getUseCageNum0() {
        return useCageNum0;
    }

    public void setUseCageNum0(Integer useCageNum0) {
        this.useCageNum0 = useCageNum0;
    }

    @Override
    public String toString() {
        return "RoleCage{" +
            "id = " + id +
            ", roleId = " + roleId +
            ", idx = " + idx +
            ", birdId = " + birdId +
            ", baseBridId = " + baseBridId +
            ", useCageId = " + useCageId +
            ", useCageNum0 = " + useCageNum0 +
        "}";
    }
}
