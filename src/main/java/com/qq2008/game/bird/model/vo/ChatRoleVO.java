package com.qq2008.game.bird.model.vo;

/***
 * 聊天玩家信息
 */
public class ChatRoleVO {
    // 角色Id
    private String roleId;
    // 角色名称
    private String roleName;
    // 角色等级
    private int level;
    // 角色VIP
    private int vip;
    // 角色称号名称
    private String titleName;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }
}
