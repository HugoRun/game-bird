package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 小鸟数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("storage_bird")
public class StorageBird implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 小鸟唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 总经验
     */
    private Long totalExp;

    /**
     * 性别
     */
    private Byte sex;

    /**
     * 初始重量, 显示时/100
     */
    private Long initWeight;

    /**
     * 当前重量, 显示时/100
     */
    private Long weight;


    Byte star;

    /**
     * 成长, 显示时/100
     */
    private Short grow;

    /**
     * 克制
     */
    private Short ko;

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

    public Long getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(Long totalExp) {
        this.totalExp = totalExp;
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

    public Short getKo() {
        return ko;
    }

    public void setKo(Short ko) {
        this.ko = ko;
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

    public Byte getStar() {
        return star;
    }

    public void setStar(Byte star) {
        this.star = star;
    }

    @Override
    public String toString() {
        return "StorageBird{" +
            "id = " + id +
            ", roleId = " + roleId +
            ", birdId = " + birdId +
            ", level = " + level +
            ", exp = " + exp +
            ", totalExp = " + totalExp +
            ", sex = " + sex +
            ", initWeight = " + initWeight +
            ", weight = " + weight +
            ", grow = " + grow +
            ", ko = " + ko +
            ", isLock = " + isLock +
            ", time = " + time +
        "}";
    }
}
