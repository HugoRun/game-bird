package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色Id
     */
    @TableId("role_id")
    private String roleId;

    /**
     * 所属用户Id
     */
    private String userId;

    /**
     * 用户名
     */
    private String roleName;

    /**
     * 当前所在场景
     */
    private Short fieldId;

    /**
     * 小鸟数量
     */
    private Short birdNum;

    /**
     * 金钱数量
     */
    private Long coin;

    /**
     * 银行储存金钱数量
     */
    private Long bankCoin;

    /**
     * 钻石数量
     */
    private Integer diamond;

    /**
     * 爱心值数量
     */
    private Long lovePoint;

    /**
     * 最后一次登录时间戳
     */
    private Integer lastLoginTime;

    /**
     * 注册时间戳
     */
    private Integer regTime;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 当前经验值
     */
    private Long exp;

    /**
     * 当前装备称号Id
     */
    private Integer titleId;

    /**
     * 婚姻类型,0无/1喜鹊之恋/2鸳鸯之恋/3比翼鸟之恋/4天使之恋
     */
    private Byte marriageType;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Short getFieldId() {
        return fieldId;
    }

    public void setFieldId(Short fieldId) {
        this.fieldId = fieldId;
    }

    public Short getBirdNum() {
        return birdNum;
    }

    public void setBirdNum(Short birdNum) {
        this.birdNum = birdNum;
    }

    public Long getCoin() {
        return coin;
    }

    public void setCoin(Long coin) {
        this.coin = coin;
    }

    public Long getBankCoin() {
        return bankCoin;
    }

    public void setBankCoin(Long bankCoin) {
        this.bankCoin = bankCoin;
    }

    public Integer getDiamond() {
        return diamond;
    }

    public void setDiamond(Integer diamond) {
        this.diamond = diamond;
    }

    public Long getLovePoint() {
        return lovePoint;
    }

    public void setLovePoint(Long lovePoint) {
        this.lovePoint = lovePoint;
    }

    public Integer getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Integer lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getRegTime() {
        return regTime;
    }

    public void setRegTime(Integer regTime) {
        this.regTime = regTime;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public Integer getTitleId() {
        return titleId;
    }

    public void setTitleId(Integer titleId) {
        this.titleId = titleId;
    }

    public Byte getMarriageType() {
        return marriageType;
    }

    public void setMarriageType(Byte marriageType) {
        this.marriageType = marriageType;
    }

    @Override
    public String toString() {
        return "Role{" +
            "roleId = " + roleId +
            ", userId = " + userId +
            ", roleName = " + roleName +
            ", fieldId = " + fieldId +
            ", birdNum = " + birdNum +
            ", coin = " + coin +
            ", bankCoin = " + bankCoin +
            ", diamond = " + diamond +
            ", lovePoint = " + lovePoint +
            ", lastLoginTime = " + lastLoginTime +
            ", regTime = " + regTime +
            ", avatarUrl = " + avatarUrl +
            ", level = " + level +
            ", exp = " + exp +
            ", titleId = " + titleId +
            ", marriageType = " + marriageType +
        "}";
    }
}
