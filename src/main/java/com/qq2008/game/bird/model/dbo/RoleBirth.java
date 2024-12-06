package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 孵化场数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_birth")
public class RoleBirth implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 孵化场唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 排序Id
     */
    private Integer idx;

    /**
     * 小鸟Id
     */
    private Integer birdId;

    /**
     * 小鸟配置Id
     */
    private Integer birdBaseId;

    /**
     * 对方玩家Id
     */
    private Integer matchRoleId;

    /**
     * 对方玩家名称
     */
    private String matchRoleName;

    /**
     * 对方小鸟唯一Id
     */
    private Integer matchBirdId;

    /**
     * 对方小鸟配置Id
     */
    private Integer matchBirdBaseId;

    /**
     * 开始配对时间戳
     */
    private Integer birthTime;

    /**
     * 配置结束时间戳
     */
    private Integer birthEndTime;

    /**
     * 使用巢穴Id
     */
    private Integer useNestId;

    /**
     * 使用巢穴剩余次数
     */
    private Integer useNestNum;

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

    public Integer getBirdBaseId() {
        return birdBaseId;
    }

    public void setBirdBaseId(Integer birdBaseId) {
        this.birdBaseId = birdBaseId;
    }

    public Integer getMatchRoleId() {
        return matchRoleId;
    }

    public void setMatchRoleId(Integer matchRoleId) {
        this.matchRoleId = matchRoleId;
    }

    public String getMatchRoleName() {
        return matchRoleName;
    }

    public void setMatchRoleName(String matchRoleName) {
        this.matchRoleName = matchRoleName;
    }

    public Integer getMatchBirdId() {
        return matchBirdId;
    }

    public void setMatchBirdId(Integer matchBirdId) {
        this.matchBirdId = matchBirdId;
    }

    public Integer getMatchBirdBaseId() {
        return matchBirdBaseId;
    }

    public void setMatchBirdBaseId(Integer matchBirdBaseId) {
        this.matchBirdBaseId = matchBirdBaseId;
    }

    public Integer getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(Integer birthTime) {
        this.birthTime = birthTime;
    }

    public Integer getBirthEndTime() {
        return birthEndTime;
    }

    public void setBirthEndTime(Integer birthEndTime) {
        this.birthEndTime = birthEndTime;
    }

    public Integer getUseNestId() {
        return useNestId;
    }

    public void setUseNestId(Integer useNestId) {
        this.useNestId = useNestId;
    }

    public Integer getUseNestNum() {
        return useNestNum;
    }

    public void setUseNestNum(Integer useNestNum) {
        this.useNestNum = useNestNum;
    }

    @Override
    public String toString() {
        return "RoleBirth{" +
            "id = " + id +
            ", roleId = " + roleId +
            ", idx = " + idx +
            ", birdId = " + birdId +
            ", birdBaseId = " + birdBaseId +
            ", matchRoleId = " + matchRoleId +
            ", matchRoleName = " + matchRoleName +
            ", matchBirdId = " + matchBirdId +
            ", matchBirdBaseId = " + matchBirdBaseId +
            ", birthTime = " + birthTime +
            ", birthEndTime = " + birthEndTime +
            ", useNestId = " + useNestId +
            ", useNestNum = " + useNestNum +
        "}";
    }
}
