package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 收藏品数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("storage_collect")
public class StorageCollect implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 小鸟唯一Id
     */
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 配置Id
     */
    private Integer birdId;

    /**
     * 等级
     */
    private Short level;

    /**
     * 经验
     */
    private Long exp;

    /**
     * 性别
     */
    private Byte sex;

    /**
     * 初始重量
     */
    private Long initWeight;

    /**
     * 当前重量
     */
    private Long weight;

    /**
     * 成长
     */
    private Short grow;

    /**
     * 克制
     */
    private Short restrained;

    /**
     * 是否锁定
     */
    private Byte isLock;

    /**
     * 捕捉时间戳
     */
    private Integer time;

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

    public Integer getBirdId() {
        return birdId;
    }

    public void setBirdId(Integer birdId) {
        this.birdId = birdId;
    }

    public Short getLevel() {
        return level;
    }

    public void setLevel(Short level) {
        this.level = level;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public Byte getSex() {
        return sex;
    }

    public void setSex(Byte sex) {
        this.sex = sex;
    }

    public Long getInitWeight() {
        return initWeight;
    }

    public void setInitWeight(Long initWeight) {
        this.initWeight = initWeight;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public Short getGrow() {
        return grow;
    }

    public void setGrow(Short grow) {
        this.grow = grow;
    }

    public Short getRestrained() {
        return restrained;
    }

    public void setRestrained(Short restrained) {
        this.restrained = restrained;
    }

    public Byte getIsLock() {
        return isLock;
    }

    public void setIsLock(Byte isLock) {
        this.isLock = isLock;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "StorageCollect{" +
            "id = " + id +
            ", roleId = " + roleId +
            ", birdId = " + birdId +
            ", level = " + level +
            ", exp = " + exp +
            ", sex = " + sex +
            ", initWeight = " + initWeight +
            ", weight = " + weight +
            ", grow = " + grow +
            ", restrained = " + restrained +
            ", isLock = " + isLock +
            ", time = " + time +
        "}";
    }
}
